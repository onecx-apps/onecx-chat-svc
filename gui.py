"""The main gui."""
from pathlib import Path
from typing import Any, List, Tuple
from dotenv import load_dotenv
import os
import streamlit as st
from loguru import logger

from agent.backend.openai_service import (
    embedd_documents_openai,
    search_documents_openai,
    chat_openai,
)

# Constants
PDF_FILE_TYPE = "pdf"
META_DATA_HEIGHT = 500
EXPLANATION_HEIGHT = 300


logger.info("Starting Application.")

# Set small icon in the tab bar
st.set_page_config(page_title="Information Retrieval Embedding Demo", page_icon=":mag:", layout="wide")

# Create title
st.title("BMI Q&A-Chatbot")

def create_folder_structure(folder_path: str) -> None:
    """Create the folder structure."""
    Path(folder_path).mkdir(parents=True, exist_ok=True)


def upload_files(save_path_input: str) -> List[Tuple[str, bytes]]:
    """Upload PDF files and save them to the file system."""
    uploaded_files = st.file_uploader("Upload PDF Files", type=PDF_FILE_TYPE, accept_multiple_files=True)
    files = []

    for file in uploaded_files:
        with open(f"{save_path_input}{file.name}", "wb") as f:
            f.write(file.getbuffer())
        files.append((file.name, file.getbuffer()))
    return files


def start_embedding(file_path: str, token: os.getenv("OPENAI_API_KEY")) -> None:
    """Start the embedding process."""
    embedd_documents_openai(dir=file_path, openai_token=token)


def search_documents(token: os.getenv("OPENAI_API_KEY"), query: str, messages: any) -> Tuple[str, str, Any]:
    """Search the documents and return the answer, prompt, and metadata."""
    documents = search_documents_openai(query=query, open_ai_token=token, amount=8)
    answer, meta_data = chat_openai(query=query, documents=documents, openai_token=token, messages=messages)
    return answer, meta_data, documents

def initialize() -> None:
    # """Initialize the GUI."""
    answer = ""
    save_path_input = "data/"
    create_folder_structure(save_path_input)

    openai_key = os.getenv("OPENAI_API_KEY")
    logger.debug("DEBUG:    API key inserted")

    with st.sidebar:
        st.subheader("Expend the bots knowledge with your pdfs:")
        # # Upload PDF files
        files = upload_files(save_path_input)

        # # Start the embedding process
        if st.button("Upload", key="start_embedding"):
            logger.debug("Embedding was started")
            start_embedding(save_path_input, openai_key)

    if "messages" not in st.session_state:
        st.session_state.messages = []
        st.session_state.messages.append({"role": "system", "content": "You are a Chat Assistant which helps the user to clarify questions. You will be provided with a document delimited by triple quotes and the users input. Your task is to help the user by using only the provided document. If the document does not contain the information needed to answer this question then simply tell the user that you cant answer the question based on the documents and then you can try to answer it without the provided document. If an answer to the question is provided, it must be annotated with a citation. Alawys reply to the user in the language provided in the users input."})
        st.session_state.messages.append({"role": "assistant", "content": "Hi wie kann ich dir helfen?"})

    for message in st.session_state.messages:
        with st.chat_message(message["role"]):
            st.markdown(message["content"])

    if prompt := st.chat_input("Was willst du fragen?"):
        
        st.session_state.messages.append({"role": "user", "content": prompt})
        with st.chat_message("user"):
            st.markdown(prompt)

        if len(st.session_state.messages) == 1:
            messages = st.session_state.messages
            answer, meta_data, documents = search_documents(openai_key, prompt, messages)
            logger.debug("DEBUG:    Before frontend bot response")
            msg = st.chat_message("assistant")
            msg.write(answer)
            logger.debug("DEBUG:    After frontend bot response")
            st.session_state.messages.append({"role": "assistant", "content": answer})
            logger.debug("DEBUG:    At the end frontend bot response")          
        else:
            messages = st.session_state.messages
            answer, meta_data, documents = search_documents(openai_key, prompt, messages)
            logger.debug("DEBUG:    Before frontend bot response")
            msg = st.chat_message("assistant")
            msg.write(answer)
            logger.debug("DEBUG:    After frontend bot response")
            st.session_state.messages.append({"role": "assistant", "content": answer})
            logger.debug("DEBUG:    At the end frontend bot response") 

            

# Start the GUI app
initialize()
load_dotenv()