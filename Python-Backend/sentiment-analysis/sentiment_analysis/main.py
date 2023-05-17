import logging

from fastapi import Depends, FastAPI
from sentiment_analysis.db.queries import fetch_product_reviews
from sentiment_analysis.dependencies import get_questionaire_db
from sqlalchemy.ext.asyncio import AsyncEngine
from transformers import pipeline
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer

app = FastAPI()

LOG = logging.getLogger(__name__)


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


@app.get("/vader_sentiment")
async def get_sentiment_analysis_vader(
    product_id: int, questionaire_db_engine: AsyncEngine = Depends(get_questionaire_db)
):
    analyzer = SentimentIntensityAnalyzer()

    data = await fetch_product_reviews(
        questionaire_db_engine=questionaire_db_engine, product_id=product_id
    )
    print(data)
    results = []
    for text in data:
        score = analyzer.polarity_scores(text)
        results.append(score)

    # Calculate total and average sentiment scores
    total_score = 0
    for score in results:
        total_score += score["compound"]
    average_score = total_score / len(results)

    # Calculate overall sentiment
    if average_score > 0.05:
        overall_sentiment = "POSITIVE"
    elif average_score < -0.05:
        overall_sentiment = "NEGATIVE"
    else:
        overall_sentiment = "NEUTRAL"

    return {
        "message": "Hello World",
        "results": results,
        "average_score": average_score,
        "overall_sentiment": overall_sentiment,
    }
