import functools
import os
from urllib.parse import quote

import pydantic
from dotenv import load_dotenv
from pydantic import Extra

load_dotenv()


class Settings(pydantic.BaseSettings):
    questionaire_db_username: str = os.getenv("QUESTIONAIRE_DB_USERNAME")
    questionaire_db_password: pydantic.SecretStr = pydantic.SecretStr(
        os.getenv("QUESTIONAIRE_DB_PASSWORD")
    )
    questionaire_db_host: str = os.getenv("QUESTIONAIRE_DB_HOST")
    questionaire_db_database_name: str = os.getenv("QUESTIONAIRE_DB_DATABASE_NAME")

    products_db_username: str = os.getenv("PRODUCTS_DB_USERNAME")
    products_db_password: pydantic.SecretStr = pydantic.SecretStr(
        os.getenv("PRODUCTS_DB_PASSWORD")
    )
    products_db_host: str = os.getenv("PRODUCTS_DB_HOST")
    products_db_database_name: str = os.getenv("PRODUCTS_DB_DATABASE_NAME")

    push_db_username: str = os.getenv("PUSH_DB_USERNAME")
    push_db_password: pydantic.SecretStr = pydantic.SecretStr(
        os.getenv("PUSH_DB_PASSWORD")
    )
    push_db_host: str = os.getenv("PUSH_DB_HOST")
    push_db_database_name: str = os.getenv("PUSH_DB_DATABASE_NAME")

    mongo_db_uri: str = os.getenv("MONGO_DB_URI")

    openai_key: str = os.getenv("OPENAI_KEY")

    class Config:
        extra = Extra.allow

    def __hash__(self):
        """
        Allow instances of this class to be hashed. Useful if we want to cache them with
        @functools.cache.

        Environment variables should not be modified at runtime, that's why we can have
        a simple hash that doesn't depend on instance fields.
        """
        return hash(f"{__name__}:{self.__class__.__name__}")

    @property
    def questionaire_url(self):  # pragma: no cover
        return f"mysql+aiomysql://{self.questionaire_db_username}:{quote(self.questionaire_db_password.get_secret_value())}@{self.questionaire_db_host}:3306/{self.questionaire_db_database_name}"

    @property
    def products_url(self):  # pragma: no cover
        return f"mysql+aiomysql://{self.products_db_username}:{quote(self.products_db_password.get_secret_value())}@{self.products_db_host}:3306/{self.products_db_database_name}"

    @property
    def push_url(self):  # pragma: no cover
        return f"mysql+aiomysql://{self.push_db_username}:{quote(self.push_db_password.get_secret_value())}@{self.push_db_host}:3306/{self.push_db_database_name}"


@functools.cache
def get_settings():  # pragma: no cover
    return Settings()
