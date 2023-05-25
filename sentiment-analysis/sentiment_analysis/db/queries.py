import logging
from typing import List

from sentiment_analysis.db.product_orm import Products, Taxonomy
from sentiment_analysis.db.questionaire_orm import Reviews
from sentiment_analysis.db.user_orm import Users, UserWooUsers
from sqlalchemy import and_, text
from sqlalchemy.ext.asyncio import AsyncEngine, AsyncSession
from sqlalchemy.sql import select

LOG = logging.getLogger(__name__)


async def fetch_taxonomy_id(products_db: AsyncEngine, taxonomy_name: str) -> int | None:
    async with AsyncSession(products_db) as session:
        result = (
            await session.execute(
                select(Taxonomy.id)
                .where(
                    Taxonomy.name == taxonomy_name,
                )
                .limit(1)
            )
        ).scalar_one_or_none()
    return result


async def fetch_taxonomy_related_products(
    products_db: AsyncEngine, taxonomy_id: int
) -> List[int]:
    async with AsyncSession(products_db) as session:
        result = (
            (
                await session.execute(
                    select(Products.id).where(
                        Products.taxonomy_id == taxonomy_id,
                    )
                )
            )
            .scalars()
            .all()
        )
    return result


async def fetch_taxonomy_of_list_of_products(
    products_db: AsyncEngine, product_ids: List[int]
) -> List[str]:
    async with AsyncSession(products_db) as session:
        result = (
            (
                await session.execute(
                    select(Taxonomy.name)
                    .join(Products, Taxonomy.id == Products.taxonomy_id)
                    .filter(Products.id.in_(product_ids))
                )
            )
            .scalars()
            .all()
        )
    return result


async def fetch_product_reviews(
    questionaire_db_engine: AsyncEngine, product_ids: List[int], user_id: int
) -> List[Reviews]:
    async with AsyncSession(questionaire_db_engine) as session:
        result = (
            (
                await session.execute(
                    select(Reviews.review_text)
                    .where(
                        and_(
                            Reviews.product_id.in_(product_ids),
                            Reviews.review_text != "",
                            Reviews.shop_user_id == user_id,
                            Reviews.rating > 4,
                        )
                    )
                    .limit(10)
                )
            )
            .scalars()
            .all()
        )
    return result


async def fetch_all_good_user_product_reviews(
    questionaire_db_engine: AsyncEngine, user_id: int
) -> List[int]:
    async with AsyncSession(questionaire_db_engine) as session:
        result = (
            (
                await session.execute(
                    select(Reviews.product_id).where(
                        and_(
                            Reviews.shop_user_id == user_id,
                            Reviews.rating > 3,
                        )
                    )
                )
            )
            .scalars()
            .all()
        )
    return result


async def fetch_all_bad_user_product_reviews(
    questionaire_db_engine: AsyncEngine, user_id: int
) -> List[int]:
    async with AsyncSession(questionaire_db_engine) as session:
        result = (
            (
                await session.execute(
                    select(Reviews.product_id).where(
                        and_(
                            Reviews.shop_user_id == user_id,
                            Reviews.rating < 4,
                        )
                    )
                )
            )
            .scalars()
            .all()
        )
    return result


async def fetch_user_survey_results(questionaire_db_engine: AsyncEngine, user_id: int):
    query = text(
        """
    SELECT a.id as answer_id, a.user_id, a.value as answer_value,
           q.id as question_id, q.value as question, q.caption as question_caption,
           q.type as question_type, q.position as question_position, q.questionnaire_id,
           o.id as answer_id, o.description as answer, o.position as option_position
    FROM answer a
      LEFT JOIN question q ON a.question_id = q.id
      LEFT JOIN radio_button_option o ON o.id = a.chosen_option_id
    WHERE q.type IN ('SELECT_SIMPLE', 'RADIO_COLOR', 'RADIO_BUTTON', 'RADIO_HTML')
      AND user_id = :user_id;
"""
    )
    # create an async session
    async with AsyncSession(questionaire_db_engine) as session:
        # execute the query
        result = await session.execute(query, {"user_id": user_id})
        # fetch the results
        answers = result.fetchall()
        # print the results

        return answers


# create an async function to execute the query
async def get_user_birthday(questionaire_db_engine: AsyncEngine, user_id: int):
    query = text(
        """
    SELECT a.user_id, a.question_id, a.value,
           q.value, q.type, q.position
    FROM answer a LEFT JOIN question q ON a.question_id = q.id
    WHERE user_id = :user_id AND q.type ='DATE_IN';
"""
    )
    async with AsyncSession(questionaire_db_engine) as session:
        # execute the query
        result = await session.execute(query, {"user_id": user_id})
        # fetch the results
        birthday = result.fetchall()
        # print the question and answer for each answer
    return birthday


async def fetch_user_state_country(push_db_engine: AsyncEngine, user_id: int) -> Users:
    async with AsyncSession(push_db_engine) as session:
        result = (
            await session.execute(
                select(Users)
                .join(UserWooUsers, UserWooUsers.user_id == Users.id)
                .where(UserWooUsers.woo_user_id == user_id)
            )
        ).scalar_one_or_none()
    return result


async def select_all_gus(gus_db_engine: AsyncEngine):
    query = text(
        """
    SELECT *
    FROM user_sentiment
    """
    )
    # create an async session
    async with AsyncSession(gus_db_engine) as session:
        # execute the query
        result = await session.execute(query)
        # fetch the results
        answers = result.fetchall()
        # print the results

    return answers
