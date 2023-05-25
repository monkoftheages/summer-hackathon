import functools

from fastapi import Depends
from pymongo.mongo_client import MongoClient
from sentiment_analysis.config import Settings, get_settings
from sqlalchemy.ext.asyncio import AsyncEngine, create_async_engine


@functools.cache
def get_questionaire_db(settings: Settings = Depends(get_settings)):  # pragma: no cover
    questionaire_db_engine: AsyncEngine = create_async_engine(
        settings.questionaire_url, pool_recycle=3600, pool_pre_ping=True, max_overflow=2
    )
    return questionaire_db_engine


@functools.cache
def get_products_db(settings: Settings = Depends(get_settings)):  # pragma: no cover
    products_db_engine: AsyncEngine = create_async_engine(
        settings.products_url, pool_recycle=3600, pool_pre_ping=True, max_overflow=2
    )
    return products_db_engine


@functools.cache
def get_push_db(settings: Settings = Depends(get_settings)):  # pragma: no cover
    push_db_engine: AsyncEngine = create_async_engine(
        settings.push_url, pool_recycle=3600, pool_pre_ping=True, max_overflow=2
    )
    return push_db_engine


@functools.cache
def get_mongo_client(settings: Settings = Depends(get_settings)):  # pragma: no cover
    return MongoClient(settings.mongo_db_uri)
