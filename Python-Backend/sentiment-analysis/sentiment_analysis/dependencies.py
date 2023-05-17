import functools

from fastapi import Depends
from sentiment_analysis.config import Settings, get_settings
from sqlalchemy.ext.asyncio import AsyncEngine, create_async_engine


@functools.cache
def get_questionaire_db(settings: Settings = Depends(get_settings)):  # pragma: no cover
    questionaire_db_engine: AsyncEngine = create_async_engine(
        settings.questionaire_url, pool_recycle=3600, pool_pre_ping=True, max_overflow=2
    )
    return questionaire_db_engine
