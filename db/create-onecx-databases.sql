/*
Create users and databases for OneCX products

IMPORTANT!
  This script cannot be executed at once!
  Pick up the section you need in a SQL session and execute it manually.
  The DROP/CREATE database statements must be executed separately.
*/

DROP DATABASE IF EXISTS "onecx-chat";
  DROP ROLE IF EXISTS onecx_chat;
  CREATE USER onecx_chat WITH PASSWORD 'onecx_chat';
CREATE DATABASE "onecx-chat" WITH OWNER = onecx_chat;

/*
ai-specific part not required if use only onecx-chat-svc
*/
DROP DATABASE IF EXISTS "onecx-ai";
  DROP ROLE IF EXISTS onecx_ai;
  CREATE USER onecx_ai WITH PASSWORD 'onecx_ai';
CREATE DATABASE "onecx-ai" WITH OWNER = onecx_ai;