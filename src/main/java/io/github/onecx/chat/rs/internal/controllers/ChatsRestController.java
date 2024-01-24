package io.github.onecx.chat.rs.internal.controllers;

import static jakarta.transaction.Transactional.TxType.NOT_SUPPORTED;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;

import gen.io.github.onecx.chat.rs.internal.ChatsInternalApi;
import gen.io.github.onecx.chat.rs.internal.model.*;
import io.github.onecx.chat.domain.daos.ChatDAO;
import io.github.onecx.chat.domain.daos.MessageDAO;
import io.github.onecx.chat.domain.models.Chat.ChatType;
import io.github.onecx.chat.domain.models.Message;
import io.github.onecx.chat.domain.models.Message.MessageType;
import io.github.onecx.chat.rs.internal.mappers.ChatMapper;
import io.github.onecx.chat.rs.internal.mappers.ExceptionMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/internal/chats") // remove this after quarkus fix ServiceExceptionMapper for impl classes
@ApplicationScoped
@Transactional(value = NOT_SUPPORTED)
public class ChatsRestController implements ChatsInternalApi {

    @Inject
    ChatDAO dao;

    @Inject
    MessageDAO msgDao;

    @Inject
    ChatMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Context
    UriInfo uriInfo;

    @Override
    @Transactional
    public Response createChat(CreateChatDTO createChatDTO) {
        var chat = mapper.create(createChatDTO);
        chat = dao.create(chat);
        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(chat.getId()).build())
                .entity(mapper.map(chat))
                .build();
    }

    @Override
    @Transactional
    public Response deleteChat(String id) {
        dao.deleteQueryById(id);
        return Response.noContent().build();
    }

    @Override
    public Response getChatById(String id) {
        var chat = dao.findById(id);
        if (chat == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(mapper.map(chat)).build();
    }

    @Override
    public Response getChats(Integer pageNumber, Integer pageSize) {
        var items = dao.findAll(pageNumber, pageSize);
        return Response.ok(mapper.mapPage(items)).build();
    }

    @Override
    public Response searchChats(ChatSearchCriteriaDTO chatSearchCriteriaDTO) {
        var criteria = mapper.map(chatSearchCriteriaDTO);
        var result = dao.findChatsByCriteria(criteria);
        return Response.ok(mapper.mapPage(result)).build();
    }

    @Override
    @Transactional
    public Response updateChat(String id, UpdateChatDTO updateChatDTO) {

        var chat = dao.findById(id);
        if (chat == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        mapper.update(updateChatDTO, chat);
        dao.update(chat);
        return Response.noContent().build();
    }

    @Override
    @Transactional
    public Response createChatMessage(String chatId, CreateMessageDTO createMessageDTO) {

        var chat = dao.findById(chatId);

        if (chat == null) {
            throw new ConstraintException("Chat does not exist", ChatErrorKeys.CHAT_DOES_NOT_EXIST, null);
        }

        var message = mapper.createMessage(createMessageDTO);
        message.setChat(chat);
        message = msgDao.create(message);

        if (ChatType.AI_CHAT.equals(chat.getType())) {
            //TODO call onecx-ai-svc rest api to generate answer
            Message aiMessage = new Message();
            aiMessage.setAppId(chat.getAppId());
            aiMessage.setChat(chat);
            aiMessage.setCreationUser(ChatType.AI_CHAT.name());
            aiMessage.setModificationUser(ChatType.AI_CHAT.name());
            aiMessage.setUserName(ChatType.AI_CHAT.name());
            aiMessage.setReliability(0.8f);
            aiMessage.setTenantId(chat.getTenantId());
            aiMessage.setText("Generated AI Answer text");
            aiMessage.setType(MessageType.ASSISTANT);
            aiMessage = msgDao.create(aiMessage);
        }

        return Response
                .created(uriInfo.getAbsolutePathBuilder().path(message.getId()).build())
                .build();

    }

    @Override
    public Response getChatMessages(String chatId) {
        var chat = dao.findById(chatId);

        if (chat == null || chat.getMessages() == null) {
            // Handle the case where chat or its messages are null
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var messages = chat.getMessages();
        List<Message> messageList = new ArrayList<>(messages);
        return Response.ok(mapper.mapList(messageList)).build();

    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    enum ChatErrorKeys {
        CHAT_DOES_NOT_EXIST
    }

}
