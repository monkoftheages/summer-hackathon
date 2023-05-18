import logging

from fastapi import Depends
from fff_fastapi import FFFFastAPI
from fff_fastapi.main import start_main
from sqlalchemy.ext.asyncio import AsyncEngine
from starlette.responses import JSONResponse
from transformers import pipeline

from sentiment_analysis.db.queries import fetch_product_reviews
from sentiment_analysis.dependencies import get_questionaire_db

app = FFFFastAPI(title="Sentiment service", auth_v2_2_docs=True)

LOG = logging.getLogger(__name__)


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


@app.get("/test")
async def test_endpoint():
    return {
        "message": "Product Sentiment Results",
    }


@app.get("/hugging_sentiment")
async def get_sentiment_analysis(
    product_id: int, questionaire_db_engine: AsyncEngine = Depends(get_questionaire_db)
):
    # siebert/sentiment-roberta-large-english
    sentiment_pipeline = pipeline(
        "sentiment-analysis", model="distilbert-base-uncased-finetuned-sst-2-english"
    )

    data = await fetch_product_reviews(
        questionaire_db_engine=questionaire_db_engine, product_id=product_id
    )
    results = sentiment_pipeline(data)

    positive_score = 0
    negative_score = 0
    review_count = len(results)

    for result in results:
        if result["label"] == "POSITIVE":
            positive_score += 1
        elif result["label"] == "NEGATIVE":
            negative_score += 1

    if negative_score > positive_score:
        total_score = (negative_score / review_count) * 100
        overall_sentiment = "NEGATIVE"
    elif negative_score < positive_score:
        total_score = (positive_score / review_count) * 100
        overall_sentiment = "POSITIVE"
    else:
        total_score = 0
        overall_sentiment = "NEUTRAL"

    return {
        "message": "Product Sentiment Results",
        "product_id": product_id,
        "total_score": f"{str(total_score)}%",
        "Overall sentiment": overall_sentiment,
    }


# @app.get("/vader_sentiment")
# async def get_sentiment_analysis_vader(
#     product_id: int, questionaire_db_engine: AsyncEngine = Depends(get_questionaire_db)
# ):
#     analyzer = SentimentIntensityAnalyzer()

#     data = await fetch_product_reviews(
#         questionaire_db_engine=questionaire_db_engine, product_id=product_id
#     )
#     print(data)
#     results = []
#     for text in data:
#         score = analyzer.polarity_scores(text)
#         results.append(score)

#     # Calculate total and average sentiment scores
#     total_score = 0
#     for score in results:
#         total_score += score["compound"]
#     average_score = total_score / len(results)

#     # Calculate overall sentiment
#     if average_score > 0.05:
#         overall_sentiment = "POSITIVE"
#     elif average_score < -0.05:
#         overall_sentiment = "NEGATIVE"
#     else:
#         overall_sentiment = "NEUTRAL"

#     return {
#         "message": "Hello World",
#         "results": results,
#         "average_score": average_score,
#         "overall_sentiment": overall_sentiment,
#     }


__all__ = ["app"]

if __name__ == "__main__":  # pragma: no cover
    start_main(app)
