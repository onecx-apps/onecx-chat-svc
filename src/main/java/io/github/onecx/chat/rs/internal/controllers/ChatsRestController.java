package io.github.onecx.chat.rs.internal.controllers;

import static jakarta.transaction.Transactional.TxType.NOT_SUPPORTED;

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
    ChatMapper mapper;

    @Inject
    ExceptionMapper exceptionMapper;

    @Context
    UriInfo uriInfo;

    @Override
    @Transactional
    public Response createNewChat(CreateChatDTO createChatDTO) {
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
    public Response getChatByType(String type) {
        var chat = dao.findChatByType(type);
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

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
