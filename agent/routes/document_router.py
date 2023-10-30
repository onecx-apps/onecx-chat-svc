import os
import aiofiles
import tempfile
from fastapi import APIRouter, UploadFile
from ..data_model.chatbot_model import DocumentDTO, UploadFileDTO
from agent.backend.openai_service import embedd_documents_openai
from loguru import logger
from typing import List
from ..backend.cloud.cloud_service_factory import CloudServiceFactory

from starlette.responses import FileResponse

document_router = APIRouter(tags=["document"])

OPENAI_API_KEY = os.environ.get("OPENAI_API_KEY")

cloud_service = CloudServiceFactory(os.getenv("CLOUD_PROVIDER"))
local_file_path = "data/"

#####qdrant rest services
@document_router.get("/embeddings/importDocuments")
def import_documents():
    logger.info("Import documents")
    
    # Example usage:
    bucket_name = os.environ.get("DOCUMENTS_BUCKET")

    # Ensure the destination folder exists
    cloud_service.download_files_from_bucket(bucket_name, local_file_path)

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

@document_router.post("/document/upload")
async def upload_document(file: UploadFile) -> UploadFileDTO:
    logger.info(f"Uploading file {file.filename} directly..")
    
    # Create temporary upload directory until uploaded file is embedded
    temp_dir = tempfile.TemporaryDirectory()
    logger.info(f"Tempdir created: {temp_dir.name}")
    destination_path = os.path.join(local_file_path, file.filename)
    
    # Write received file to disk chunked
    async with aiofiles.open(destination_path, "wb") as out_file:
        while content := await file.read(1024):
            await out_file.write(content)
    
    embedd_documents_openai(dir=temp_dir.name, openai_token=OPENAI_API_KEY)

    # Cleanup
    temp_dir.cleanup()
    return UploadFileDTO(filename=file.filename, size=file.file.tell(), content_type=file.content_type)

@document_router.post("/document/uploadMultiple/{conversationId}")
async def upload_documents(conversationId: str, documents: List[UploadFile]) -> List[UploadFileDTO]:
    response = []
    for document in documents:
        response.append(await upload_document(document))
    
    return response