import os


from qdrant_client import QdrantClient, models
from qdrant_client.http.models.models import UpdateResult

from loguru import logger

def get_qdrant_client() -> QdrantClient:
    """Initializes the OpenAi vector db.

    Args:
        cfg (DictConfig): Configuration from the file
    """
    qdrant_client = QdrantClient(os.getenv("QDRANT_URL"), port=os.getenv("QDRANT_PORT"), api_key=os.getenv("QDRANT_API_KEY"))
  
    try:

        collection_name = "llama2"
        qdrant_client.get_collection(collection_name)
        logger.info(f"SUCCESS: Collection {collection_name} already exists.")
    except Exception:
        qdrant_client.recreate_collection(
            collection_name=collection_name,
            vectors_config=models.VectorParams(size=4096, distance=models.Distance.COSINE),
        )
        logger.info(f"SUCCESS: Collection {collection_name} created.")

