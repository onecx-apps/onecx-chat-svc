import os
from typing import Any, Dict, List, Optional, Tuple, Union

import openai
from dotenv import load_dotenv
from langchain.docstore.document import Document
from langchain.document_loaders import DirectoryLoader, PyPDFLoader
from langchain.embeddings.openai import OpenAIEmbeddings
from langchain.vectorstores import Qdrant
from loguru import logger
from omegaconf import DictConfig
from qdrant_client import QdrantClient
from agent.utils.configuration import load_config
from agent.utils.utility import generate_prompt
from langchain.docstore.document import Document as LangchainDocument
from langchain.text_splitter import CharacterTextSplitter
from langchain.embeddings import OpenAIEmbeddings
from qdrant_client.http import models
import logging
from agent.backend.qdrant_service import get_qdrant_client


from langchain.embeddings import OllamaEmbeddings
from langchain.chat_models import ChatOllama
from langchain.callbacks.manager import CallbackManager
from langchain.callbacks.streaming_stdout import StreamingStdOutCallbackHandler                                  




logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

load_dotenv()


channeling_system_message = """
Please be my assistant in navigating through the registration process.

The process has the following fixed steps beginning with the step id: followed by the step name

1: Full name of USER which consists of surname and lastname
2: Is USER an EU citizen?:
   2a: If USER is an EU citizen, then no registration is necessary and the process is finished!
   2b: If USER is not an EU citizen then ask which is the homeland, in case the homeland was not mentioned by the USER already.
    3a: If the homeland is in the EU, then no registration is necessary!
    3b: If the homeland is outside the EU, ask the USER where the EU border was crossed.
        3b1: If USER provided an (eu-entry-country) which is an EU country, then finish the registration process with step id 5 and an additional hint that the registration should be done in the (eu-entry-country):
        3b2: If USER did not cross the EU border, ASSISTANT should finish the process with an additional hint about the german visa process!
        3b3: IF USER provided an (eu-entry-country) which is not an EU country ask step 3b again.
4: After the process is finished make a summary of the USER with the following fields: Full name, Homeland, EU entry country


Please go through the process step by step from beginning to end and ask individual questions sequentially to determine which path in the registration process needs to be followed. Do not ask all questions at once. Check the answers for plausibility and if it is not repeat the question!
The step id is only used for navigation but should not be displayed.



The following list represents the EU countries:
Belgium,Bulgaria,Denmark,Germany,Estonia,Finland,France,Greece,Ireland,Italy,Croatia,Latvia,Lithuania,Luxembourg,Malta,Netherlands,Austria,Poland,Portugal,Romania,Sweden,Slovakia,Slovenia,Spain,Czech Republic,Hungary,Cyprus

"""

q_and_a_system_message = """
You are a Chat Assistant which helps the user to clarify questions.

You will be provided with a document delimited by triple quotes and the users input.
Your task is to help the user by using only the provided document.
If the document does not contain the information needed to answer this question then simply tell the user that you cant answer the question based on the additional provided knowledge and then you answer it without the provided document.
Alawys reply to the user in the language provided in the users input.
Always delete special characters that are used in HTML syntax like the newline characters to make it look more like a human response.

"""



@load_config(location="config/db.yml")
def get_db_connection(cfg: DictConfig) -> Qdrant:
    """get_db_connection initializes the connection to the Qdrant db.

    :param cfg: OmegaConf configuration
    :type cfg: DictConfig
    :return: Qdrant DB connection
    :rtype: Qdrant
    """
    embedding = OllamaEmbeddings(base_url="http://localhost:11434", model="llama2")
    qdrant_client = QdrantClient(os.getenv("QDRANT_URL",cfg.qdrant.url), port=os.getenv("QDRANT_PORT",cfg.qdrant.port), api_key=os.getenv("QDRANT_API_KEY"), prefer_grpc=cfg.qdrant.prefer_grpc)
    try: 
        qdrant_client.get_collection(collection_name=cfg.qdrant.collection_name_llama2)  
        
    except Exception:
        qdrant_client.recreate_collection(
            collection_name=cfg.qdrant.collection_name_llama2,
            vectors_config=models.VectorParams(size=1536, distance=models.Distance.COSINE),
        )
        logger.info(f"SUCCESS: Collection {cfg.qdrant.collection_name_llama2} created.")
    vector_db = Qdrant(client=qdrant_client, collection_name=cfg.qdrant.collection_name_llama2, embeddings=embedding)
    logger.info("SUCCESS: Qdrant DB Connection.")
    return vector_db


@load_config(location="config/ai/llama2.yml")
def summarize_text_llama2(text: str, token: str, cfg: DictConfig) -> str:
    """Summarizes the given text using the llama2 API.

    Args:
        text (str): The text to be summarized.
        token (str): The token for the llama2 API.

    Returns:
        str: The summary of the text.
    """
    prompt = generate_prompt(prompt_name="llama2-summarization.j2", text=text, language="de")

    llm = ChatOllama(
    base_url="http://localhost:11434",
    model="llama2",
    verbose=True
    )

    response = llm(prompt)

    return response


def embedd_documents_llama2(dir: str) -> None:
    """Embeds the documents in the given directory in the llama2 database.

    This method uses the Directory Loader for PDFs and the PyPDFLoader to load the documents.
    The documents are then added to the Qdrant DB which embeds them without deleting the old collection.

    Args:
        dir (str): The directory containing the PDFs to embed.

    Returns:
        None
    """
    vector_db = get_db_connection()

    logger.info(f"Logged directory:  {dir}")
    loader = DirectoryLoader(dir, glob="*.pdf", loader_cls=PyPDFLoader)
    length_function = len
    splitter = CharacterTextSplitter(
        chunk_size=250,
        chunk_overlap=50,
        length_function=length_function,
    )
    docs = loader.load_and_split(splitter)
    logger.info(f"Loaded {len(docs)} documents.")
    text_list = [doc.page_content for doc in docs]
    metadata_list = [doc.metadata for doc in docs]
    vector_db.add_texts(texts=text_list, metadatas=metadata_list)
    logger.info("SUCCESS: Texts embedded.")


def embedd_text_llama2(text: str, file_name: str, seperator: str) -> None:
    """Embeds the given text in the llama2 database.

    Args:
        text (str): The text to be embedded.


    Returns:
        None
    """
    vector_db = get_db_connection()

    # split the text at the seperator
    text_list: List = text.split(seperator)

    # check if first and last element are empty
    if not text_list[0]:
        text_list.pop(0)
    if not text_list[-1]:
        text_list.pop(-1)

    metadata = file_name
    # add _ and an incrementing number to the metadata
    metadata_list: List = [{"source": f"{metadata}_{str(i)}", "page": 0} for i in range(len(text_list))]

    vector_db.add_texts(texts=text_list, metadatas=metadata_list)
    logger.info("SUCCESS: Text embedded.")


def embedd_text_files_llama2(folder: str, seperator: str) -> None:
    """Embeds text files in the llama2 database.

    Args:
        folder (str): The folder containing the text files to embed.
        seperator (str): The seperator to use when splitting the text into chunks.

    Returns:
        None
    """
    vector_db = get_db_connection()

    # iterate over the files in the folder
    for file in os.listdir(folder):
        # check if the file is a .txt or .md file
        if not file.endswith((".txt", ".md")):
            continue

        # read the text from the file
        with open(os.path.join(folder, file)) as f:
            text = f.read()

        text_list: List = text.split(seperator)

        # check if first and last element are empty
        if not text_list[0]:
            text_list.pop(0)
        if not text_list[-1]:
            text_list.pop(-1)

        # ensure that the text is not empty
        if not text_list:
            raise ValueError("Text is empty.")

        logger.info(f"Loaded {len(text_list)} documents.")
        # get the name of the file
        metadata = os.path.splitext(file)[0]
        # add _ and an incrementing number to the metadata
        metadata_list: List = [{"source": f"{metadata}_{str(i)}", "page": 0} for i in range(len(text_list))]
        vector_db.add_texts(texts=text_list, metadatas=metadata_list)

    logger.info("SUCCESS: Text embedded.")
    


def search_documents_llama2(query: str, amount: int, collection_name: Optional[str] = None) -> List[Tuple[Document, float]]:
    """Searches the documents in the Qdrant DB with a specific query.

    Args:
        query (str): The question for which documents should be searched.

    Returns:
        List[Tuple[Document, float]]: A list of search results, where each result is a tuple
        containing a Document object and a float score.
    """
    vector_db = get_db_connection()

    docs = vector_db.similarity_search_with_score(query, k=amount)
    logger.info("SUCCESS: Documents found.")

    logger.info(f"These are the docs found after similarity_search_with_score: {docs}")

    return docs

@load_config(location="config/ai/llama2.yml")
def send_chat_completion_llama2(text: str, query: str, cfg: DictConfig, conversation_type: str, messages: any) -> str:
    """Sent completion request to llama2 API.

    Args:
        text (str): The text on which the completion should be based.
        query (str): The query for the completion.
        cfg (DictConfig):

    Returns:
        str: Response from the llama2 API.
    """

    #fill the prompt if not q and a then it will use channeling with the documents
    if conversation_type == "CHANNELING":
        prompt = generate_prompt(prompt_name="llama2-channeling.j2", text=text, query=query, language="de")
    else:
        prompt = generate_prompt(prompt_name="llama2-qa.j2", text=text, query=query, language="de")
    messages.append({"role": "user", "content": prompt})
    logger.info(f"DEBUG: This is the filled prompt before request: {prompt}")

    llm = ChatOllama(
    base_url="http://localhost:11434",
    model="llama2",
    verbose=True
    )

    response = llm.predictMessages(
        messages=[{"role": m["role"], "content": m["content"]} for m in messages],
    )

    logger.debug(response)
    return response["choices"][0]["message"]["content"]

def chat_llama2(documents: list[tuple[LangchainDocument, float]], messages: any, query: str, conversation_type: str, summarization: bool = False) -> Tuple[str, Union[Dict[Any, Any], List[Dict[Any, Any]]]]:
    """QA takes a list of documents and returns a list of answers.

    Args:
        documents (List[Tuple[Document, float]]): A list of tuples containing the document and its relevance score.
        query (str): The query to ask.
        summarization (bool, optional): Whether to use summarization. Defaults to False.

    Returns:
        Tuple[str, str, Union[Dict[Any, Any], List[Dict[Any, Any]]]]: A tuple containing the answer, the prompt, and the metadata for the documents.
    """
    text = ""
    if conversation_type == "Q_AND_A":
        # if the list of documents contains only one document extract the text directly
        if len(documents) == 1:
            text = documents[0][0].page_content
            meta_data = documents[0][0].metadata

        else:
            # extract the text from the documents
            texts = [doc[0].page_content for doc in documents]
            if summarization:
                # call summarization
                text = ""
                for t in texts:
                    text += summarize_text_llama2(t)

            else:
                # combine the texts to one text
                text = " ".join(texts)
            meta_data = [doc[0].metadata for doc in documents]
    else:
        text = ""
        meta_data=[]
    
    answer=""
    try:
        # call the gpt api
        answer = send_chat_completion_llama2(text=text, query=query, conversation_type=conversation_type, messages=messages)
        logger.info(f"DEBUG: This is the answer after request: {answer}")

    except ValueError as e:
        # if the code is PROMPT_TOO_LONG, split it into chunks
        
        logger.info("DEBUG: Error found. Summarizing again.")

        # summarize the text
        short_text = summarize_text_llama2(text)

        # generate the prompt
        prompt = generate_prompt("llama2-qa.j2", text=short_text, query=query)

        # call the luminous api
        answer = summarize_text_llama2(prompt)

    # extract the answer
    #logger.info(f"DEBUG: This is the final answer: {answer}")
    
    return answer, meta_data




if __name__ == "__main__":
    print("Test")


