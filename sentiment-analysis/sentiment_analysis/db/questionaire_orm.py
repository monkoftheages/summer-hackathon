from sqlalchemy import Column, Integer, String
from sqlalchemy.orm import declarative_base

Base = declarative_base()


class Reviews(Base):
    __tablename__ = "reviews"

    id: int = Column(Integer, primary_key=True)
    product_id: int = Column(Integer)
    review_text: str = Column(String)
    shop_user_id: int = Column(Integer)
    rating: int = Column(Integer)

    def __repr__(self):
        return f"Reviews(id={self.id}, product_id={self.product_id}, reviews={self.reviews})"
