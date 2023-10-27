import os
from fastapi import APIRouter
from ..data_model.chatbot_model import DocumentDTO
from agent.backend.google_cloud_service import download_files_from_gcs
from agent.backend.openai_service import embedd_documents_openai
from loguru import logger
from typing import List

document_router = APIRouter(tags=["document"])

OPENAI_API_KEY = os.environ.get("OPENAI_API_KEY")


@document_router.post("/uploadDocuments/{conversationId}")
async def upload_documents(conversationId: str, documents: List[DocumentDTO]):
    # Implement your logic here
    return {"message": "Documents uploaded successfully"}


#####qdrant rest services
@document_router.get("/embeddings/importDocuments")
def import_documents():
    logger.info("Import documents")

    # Example usage:
    bucket_name = os.environ.get("DOCUMENTS_BUCKET")
    local_file_path = "data/"

    # Ensure the destination folder exists
    download_files_from_gcs(bucket_name, local_file_path)

    embedd_documents_openai(dir=local_file_path, openai_token=OPENAI_API_KEY)

    # Cleanup: Remove existing files in the destination folder
    for file_name in os.listdir(local_file_path):
        file_path = os.path.join(local_file_path, file_name)
        try:
            if os.path.isfile(file_path):
                os.unlink(file_path)
        except Exception as e:
            print(f"Error deleting {file_path}: {e}")

    return {"message": "Documents are imported into vector db"}
