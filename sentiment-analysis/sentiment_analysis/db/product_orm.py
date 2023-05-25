from sqlalchemy import Column, Integer, String
from sqlalchemy.orm import declarative_base

Base = declarative_base()


class Products(Base):
    __tablename__ = "products"

    id: int = Column(Integer, primary_key=True)
    taxonomy_id: int = Column(Integer)

    def __repr__(self):
        return f"Reviews(id={self.id}, product_id={self.taxonomy_id}"


class Taxonomy(Base):
    __tablename__ = "taxonomies"

    id: int = Column(Integer, primary_key=True)
    name: int = Column(String)

    def __repr__(self):
        return f"Taxonomy(id={self.id}, name={self.name})"
