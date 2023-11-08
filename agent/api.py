"""FastAPI Backend for the Knowledge Agent."""
import json
import os
import uuid
from typing import List, Optional
import time

from dotenv import load_dotenv
from fastapi import Body, FastAPI, File, HTTPException, UploadFile, Path

from agent.data_model.chatbot_model import ChatMessageDTO, ConversationDTO, ConversationType, DocumentDTO, MessageType
from langchain.docstore.document import Document as LangchainDocument
from loguru import logger

from agent.backend.google_cloud_service import (
    download_files_from_gcs,
)


from agent.backend.qdrant_service import (
    get_qdrant_client
)


from agent.backend.llama2_service import (
    embedd_documents_llama2,
    search_documents_llama2,
    chat_llama2,
    channeling_system_message,
    q_and_a_system_message
)



from agent.data_model.qdrant_model import (
    CustomPromptCompletion,
    EmbeddTextFilesRequest,
    EmbeddTextRequest,
    ExplainRequest,
    QARequest,
    SearchRequest,
    SearchResponse,
)
from agent.utils.configuration import load_config


# add file logger for loguru
logger.add("logs/file_{time}.log", backtrace=False, diagnose=False)
logger.info("Startup.")


# initialize the Fast API Application.
app = FastAPI(debug=True)
load_dotenv()
logger.info("Loading REST API Finished.")

chatConversationMemory = []
def get_chat_by_conversation_id(conversationId):
    for conversation in chatConversationMemory:
        if conversation["conversationId"] == conversationId:
            return conversation
    return None

#function that returns a ConversationDTO but without System Messages
def get_chat_by_conversation_id_filtered(conversationId):
    for conversation in chatConversationMemory:
        if conversation["conversationId"] == conversationId:
            conversationObj = ConversationDTO(conversationId=conversation["conversationId"], history=conversation["history"], conversationType=ConversationType(conversation["conversationType"]))
            
            # Code to filter out the SYSTEM messages
            conversationObj.history = [msg for msg in conversationObj.history if msg.type != MessageType.SYSTEM]
            
            return conversationObj  # Return the filtered conversation object
    return None


@app.get("/")
def read_root() -> str:
    """Returns the welcome message.

    Returns:
        str: The welcome message.
    """
    return "Welcome to the BMI Chatbot Backend!"


@app.post("/chat")
async def chat_with_bot(chat_message: ChatMessageDTO) -> ChatMessageDTO:
    # Check if conversation exists
    conversation = get_chat_by_conversation_id(chat_message.conversationId)
    
    # Convert ChatMessageDTO to a dict format to append to history
    message_dict = chat_message.dict()
    message_dict["correlationId"] = str(uuid.uuid4())
    message_dictDTO = ChatMessageDTO(conversationId=message_dict["conversationId"], correlationId=message_dict["correlationId"], message=message_dict["message"], type=message_dict["type"], creationDate=int(time.time()))

    if not conversation:
        # If conversation doesn't exist, raise an error
        raise HTTPException(status_code=404, detail="Conversation not found")
    
    # array for llama2 chat completion request - chat history with system message
    chatCompletionArr = []
    logger.debug(f"Here is conversation history:     {conversation['history']}")
    for msg in conversation["history"]:
        logger.debug(f"Here is msg:     {msg.type}")
        chatCompletionArr.append({"role": msg.type, "content": msg.message})

    # If conversation exists, append the message to its history
    conversation["history"].append(message_dictDTO)

    #response bot
    documents = search_documents_llama2(query=message_dict["message"], amount=1)
    answer, meta_data = chat_llama2(query=message_dict["message"], documents=documents, conversation_type=conversation["conversationType"], messages=chatCompletionArr)
    botResponse = ChatMessageDTO(conversationId=chat_message.conversationId, correlationId=message_dict["correlationId"], message=answer, type=MessageType.ASSISTANT, creationDate=int(time.time()))
    conversation["history"].append(botResponse)
    return botResponse

@app.get("/conversation/{conversationId}")
async def get_conversation(conversationId: str) -> ConversationDTO:
    # Implement your logic here

    conversation = get_chat_by_conversation_id_filtered(conversationId)
    
    if  conversation == None:
        raise HTTPException(status_code=404, detail="Conversation not found")
    else:
        return conversation






@app.post("/startConversation")
async def start_conversation(conversation_type: str = Body(..., embed=True)) -> ConversationDTO:
    conversation_id_uuid = str(uuid.uuid4())
    start_conversation = []
    if conversation_type == "CHANNELING":
        # iniialSystemMessage = ChatMessageDTO(conversationId=conversation_id_uuid, correlationId="System Message", message=channeling_system_message, type=MessageType.SYSTEM, creationDate=int(time.time()))
        # start_conversation.append(iniialSystemMessage)
        welcomeMessage= ChatMessageDTO(conversationId=conversation_id_uuid, correlationId="System Message", message="Hallo ich bin dein Asisstent und führe dich durch den Anmeldeprozess. Wie ist dein Name?", type=MessageType.ASSISTANT, creationDate=int(time.time()))
        start_conversation.append(welcomeMessage)
    if conversation_type == "Q_AND_A":
        # iniialSystemMessage = ChatMessageDTO(conversationId=conversation_id_uuid, correlationId="System Message", message=q_and_a_system_message, type=MessageType.SYSTEM, creationDate=int(time.time()))
        # start_conversation.append(iniialSystemMessage)
        welcomeMessage= ChatMessageDTO(conversationId=conversation_id_uuid, correlationId="System Message", message="Hallo ich bin dein Asisstent für heute! Was möchtest du wissen?(Q&A)", type=MessageType.ASSISTANT, creationDate=int(time.time()))
        start_conversation.append(welcomeMessage)

    chatConversationMemory.append({"conversationId": conversation_id_uuid, "history": start_conversation, "conversationType": conversation_type})

    conversation = get_chat_by_conversation_id_filtered(conversation_id_uuid)
    
    if  conversation == None:
        raise HTTPException(status_code=404, detail="Conversation not found")
    else:
        return conversation
    

@app.post("/uploadDocuments/{conversationId}")
async def upload_documents(conversationId: str, documents: List[DocumentDTO]):
    # Implement your logic here
    return {"message": "Documents uploaded successfully"}


#####qdrant rest services

@app.get("/embeddings/importDocuments")
def import_documents():

    logger.info("Import documents")

    # Example usage:
    bucket_name = os.environ.get("DOCUMENTS_BUCKET")
    local_file_path = "data/"

    # Ensure the destination folder exists
    download_files_from_gcs(bucket_name, local_file_path)

    
    embedd_documents_llama2(dir=local_file_path)

    # Cleanup: Remove existing files in the destination folder
    for file_name in os.listdir(local_file_path):
        file_path = os.path.join(local_file_path, file_name)
        try:
            if os.path.isfile(file_path):
                os.unlink(file_path)
        except Exception as e:
            print(f"Error deleting {file_path}: {e}")


    return {"message": "Documents are imported into vector db"}




# initialize the databases
get_qdrant_client()
