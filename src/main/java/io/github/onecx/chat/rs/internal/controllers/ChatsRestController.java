package io.github.onecx.chat.rs.internal.controllers;

import static jakarta.transaction.Transactional.TxType.NOT_SUPPORTED;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Response;

import gen.io.github.onecx.chat.rs.internal.ChatsInternalApi;
import gen.io.github.onecx.chat.rs.internal.model.*;
import io.github.onecx.chat.rs.internal.services.ChatsService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@Transactional(value = NOT_SUPPORTED)
public class ChatsRestController implements ChatsInternalApi {

    @Inject
    ChatsService service;

    @Override
    public Response createChat(CreateChatDTO createChatDTO) {
        return service.createChat(createChatDTO);
    }

    @Override
    public Response deleteChat(String id) {
        return service.deleteChat(id);
    }

    @Override
    public Response getChatById(String id) {
        return service.getChatById(id);
    }

    @Override
    public Response getChats(Integer pageNumber, Integer pageSize) {
        return service.getChats(pageNumber, pageSize);
    }

    @Override
    public Response searchChats(ChatSearchCriteriaDTO chatSearchCriteriaDTO) {
        return service.searchChats(chatSearchCriteriaDTO);
    }

    @Override
    public Response updateChat(String id, UpdateChatDTO updateChatDTO) {
        return service.updateChat(id, updateChatDTO);
    }

    @Override
    public Response createChatMessage(String chatId, CreateMessageDTO createMessageDTO) {
        return service.createChatMessage(chatId, createMessageDTO);
    }

    @Override
    public Response getChatMessages(String chatId) {
        return service.getChatMessages(chatId);

    }

    @Override
    public Response addParticipant(String chatId, @Valid @NotNull AddParticipantDTO addParticipantDTO) {
        return service.addParticipant(chatId, addParticipantDTO);
    }

    @Override
    public Response getChatParticipants(String chatId) {
        return service.getChatParticipants(chatId);
    }
}
