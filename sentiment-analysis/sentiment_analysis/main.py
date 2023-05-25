import asyncio
import logging
import re
from datetime import datetime

import openai
from fastapi import BackgroundTasks, Depends
from fff_fastapi import FFFFastAPI
from fff_fastapi.main import start_main
from pydantic import BaseModel
from pymongo.mongo_client import MongoClient
from sentiment_analysis.config import Settings, get_settings
from sentiment_analysis.db.queries import (
    fetch_all_bad_user_product_reviews,
    fetch_all_good_user_product_reviews,
    fetch_taxonomy_of_list_of_products,
    fetch_user_state_country,
    fetch_user_survey_results,
    get_user_birthday,
)
from sentiment_analysis.dependencies import (
    get_mongo_client,
    get_products_db,
    get_push_db,
    get_questionaire_db,
)
from sqlalchemy.ext.asyncio import AsyncEngine
from starlette.responses import JSONResponse

app = FFFFastAPI(title="Sentiment service", auth_v2_2_docs=True)
LOG = logging.getLogger(__name__)


class SentimentResponse(BaseModel):
    sentiment: int


class Request(BaseModel):
    user_id: int
    user_query_id: str
    user_query: str


@app.exception_handler(Exception)
def default_exception_handler(_, e: Exception):
    LOG.error("Error processing request", exc_info=e)
    return JSONResponse(
        status_code=500,
        content={
            "code": "internal-server-error",
            "message": "Internal Server Error",
        },
    )


@app.post("/hugging_sentiment")
async def get_sentiment_analysis(
    user_query: Request,
    background_tasks: BackgroundTasks,
    project_settings: Settings = Depends(get_settings),
    questionaire_db_engine: AsyncEngine = Depends(get_questionaire_db),
    products_db_engine: AsyncEngine = Depends(get_products_db),
    push_db_engine: AsyncEngine = Depends(get_push_db),
    mongo_client: MongoClient = Depends(get_mongo_client),
):
    all_good_user_reviews, all_bad_user_reviews = await asyncio.gather(
        fetch_all_good_user_product_reviews(
            questionaire_db_engine=questionaire_db_engine, user_id=user_query.user_id
        ),
        fetch_all_bad_user_product_reviews(
            questionaire_db_engine=questionaire_db_engine, user_id=user_query.user_id
        ),
    )

    all_good_reviews_taxonomies, all_bad_reviews_taxonomies = await asyncio.gather(
        fetch_taxonomy_of_list_of_products(
            products_db=products_db_engine, product_ids=all_good_user_reviews
        ),
        fetch_taxonomy_of_list_of_products(
            products_db=products_db_engine, product_ids=all_bad_user_reviews
        ),
    )

    # get user info
    # user survey data
    user_survey_data = await fetch_user_survey_results(
        questionaire_db_engine=questionaire_db_engine, user_id=user_query.user_id
    )

    # user age
    user_date_of_birth = await get_user_birthday(
        questionaire_db_engine=questionaire_db_engine, user_id=user_query.user_id
    )
    # user address
    user_state_country = await fetch_user_state_country(
        push_db_engine=push_db_engine, user_id=user_query.user_id
    )
    user_birthday = "Unknown"

    for row in user_date_of_birth:
        user_birthday = row[2]
    question_answers = []
    for answer in user_survey_data:
        question_answers.append(f"Question: {answer.question} Answer: {answer.answer}")

    openai.api_key = project_settings.openai_key
    # call chat gpt
    messages = [
        {
            "role": "system",
            "content": f"You are a intelligent AI assistant. I will send information about a customer. This will include a list of survey questions they were asked and their answers and other general information about the user such as birthday, where they live, any good/bad product reviews and more. Reply to me with a percentage of probability in regards to the question asked by the user in the first line regarding the customer. Use a propensity model to help calculate. Reply with ONLY the percentage and no further words or characters. For example a reply will look like '90'",
        }
    ]
    messages.append(
        {
            "role": "user",
            "content": f"Based on the below information, {user_query.user_query} . \n"
            + f"- Users Birthday is: {user_birthday} \n"
            + f"- Survey Question and answers list for this user: {question_answers} \n"
            + f"- List of product categories the user gave postivie reviews for: {all_good_reviews_taxonomies} \n"
            + f"- List of product categories the user gave negative reviews for: {all_bad_reviews_taxonomies} \n"
            + f"- User lives in: {user_state_country.ship_state},{user_state_country.ship_country}.",
        },
    )
    int_sentiment = 0
    try:
        chat = openai.ChatCompletion.create(
            model="gpt-3.5-turbo", messages=messages, timeout=60
        )
        ai_analysis_percent = chat.choices[0].message.content

        db = mongo_client["user_sentiment"]
        user_collection = db["user_sentiment"]

        int_sentiment = int(
            re.sub("\D", "", ai_analysis_percent.strip().replace("%", ""))
        )
        print(f"ChatGPT - sentiemnt {int_sentiment}")
    except Exception:
        print("Unknown error")

    document = {
        "user_id": str(user_query.user_id),
        "question_id": user_query.user_query_id,
        "sentiment": int_sentiment,
    }
    result = user_collection.insert_one(document)
    print("Inserted document ID:", result.inserted_id)

    users_age = await calculate_age(user_birthday)
    background_tasks.add_task(
        upload_user_segment_average,
        mongo_client,
        users_age,
        user_state_country.ship_state,
        int_sentiment,
        user_query.user_query,
    )

    return int_sentiment


async def upload_user_segment_average(
    mongo_client: MongoClient,
    age: int,
    user_state: str,
    user_sentiment: int,
    user_query: str,
):
    try:
        db = mongo_client["user_sentiment"]
        segment_collection = db["segment_average"]

        age_group = None
        if age == 0:
            return
        elif age < 20:
            age_group = "16-19"
        elif age < 30:
            age_group = "20-29"
        elif age < 40:
            age_group = "30-39"
        elif age < 50:
            age_group = "40-49"
        elif age < 60:
            age_group = "50-59"
        elif age < 70:
            age_group = "60-69"
        else:
            age_group = "70+"

        document = segment_collection.find_one(
            {
                "age_group": age_group,
                "state": user_state,
                "query_question": user_query,
            }
        )
        if document:
            # Increment the integer value
            new_value = document["count"] + 1
            new_sentiment_average = (
                document["sentiment"] * document["count"] + user_sentiment
            ) / (new_value)
            object_id = document["_id"]

            # Update the document in the collection
            segment_collection.update_one(
                {"_id": object_id},
                {"$set": {"sentiment": new_sentiment_average, "count": new_value}},
            )
            print("Average updated successfully.")
        else:
            document = {
                "age_group": age_group,
                "state": user_state,
                "query_question": user_query,
                "sentiment": user_sentiment,
                "count": 1,
            }
            result = segment_collection.insert_one(document)
            print(f"New segment group added {result}")
    except Exception:
        print("Small background task error")


async def calculate_age(dob: str) -> int:
    try:
        # Convert the string birthdate to a datetime object
        birthdate = datetime.strptime(dob, "%Y-%m-%dT%H:%M:%S.%fZ")

        # Get the current date
        current_date = datetime.now()

        # Calculate the age
        age = current_date.year - birthdate.year

        # Adjust the age if the birthdate hasn't occurred yet this year
        if current_date.month < birthdate.month or (
            current_date.month == birthdate.month and current_date.day < birthdate.day
        ):
            age -= 1

        return age
    except Exception:
        return 0


__all__ = ["app"]

if __name__ == "__main__":  # pragma: no cover
    start_main(app)
