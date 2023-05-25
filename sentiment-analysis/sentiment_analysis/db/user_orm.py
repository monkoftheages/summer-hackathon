from sqlalchemy import Column, Integer, String
from sqlalchemy.orm import declarative_base

Base = declarative_base()


class Users(Base):
    __tablename__ = "users"

    id: int = Column(Integer, primary_key=True)
    ship_state: str = Column(String)
    ship_country: str = Column(String)

    def __repr__(self):
        return f"Reviews(id={self.id}, product_id={self.taxonomy_id}"


class UserWooUsers(Base):
    __tablename__ = "user_woo_users"

    id: int = Column(Integer, primary_key=True)
    user_id: int = Column(Integer)
    woo_user_id: int = Column(Integer)

    def __repr__(self):
        return f"UserWoo(id={self.id}, user_id={self.user_id})"
