import logging
from typing import List

from sentiment_analysis.db.questionaire_orm import Reviews
from sqlalchemy import and_
from sqlalchemy.ext.asyncio import AsyncEngine, AsyncSession
from sqlalchemy.sql import select

LOG = logging.getLogger(__name__)


async def fetch_product_reviews(
    questionaire_db_engine: AsyncEngine, product_id: int
) -> List[Reviews]:
    async with AsyncSession(questionaire_db_engine) as session:
        result = (
            (
                await session.execute(
                    select(Reviews.review_text)
                    .where(
                        and_(
                            Reviews.product_id == product_id,
                            Reviews.review_text != "",
                        )
                    )
                    .limit(15)
                )
            )
            .scalars()
            .all()
        )
    return result
