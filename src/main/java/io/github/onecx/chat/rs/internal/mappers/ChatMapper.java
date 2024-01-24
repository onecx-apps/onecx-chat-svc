package io.github.onecx.chat.rs.internal.mappers;

import java.util.List;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gen.io.github.onecx.chat.rs.internal.model.*;
import io.github.onecx.chat.domain.criteria.ChatSearchCriteria;
import io.github.onecx.chat.domain.models.Chat;
import io.github.onecx.chat.domain.models.Message;

@Mapper(uses = { OffsetDateTimeMapper.class })
public abstract class ChatMapper {

    @Inject
    ObjectMapper mapper;

    public abstract ChatSearchCriteria map(ChatSearchCriteriaDTO dto);

    @Mapping(target = "removeStreamItem", ignore = true)
    public abstract ChatPageResultDTO mapPage(PageResult<Chat> page);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "messages", ignore = true)
    public abstract Chat create(CreateChatDTO object);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "chat", ignore = true)
    public abstract Message createMessage(CreateMessageDTO dto);

    public abstract List<ChatDTO> map(Stream<Chat> entity);

    @Mapping(target = "version", source = "modificationCount")
    public abstract ChatDTO map(Chat chat);

    @Mapping(target = "version", source = "modificationCount")
    public abstract MessageDTO map(Message chat);

    public abstract List<MessageDTO> mapList(List<Message> items);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "creationUser", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "modificationUser", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "messages", ignore = true)
    public abstract void update(UpdateChatDTO chatDTO, @MappingTarget Chat entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "messages", ignore = true)
    public abstract Chat map(UpdateChatDTO object);

    @Named("properties")
    public String mapToString(Object properties) {

        if (properties == null) {
            return null;
        }

        try {
            return mapper.writeValueAsString(properties);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

}
