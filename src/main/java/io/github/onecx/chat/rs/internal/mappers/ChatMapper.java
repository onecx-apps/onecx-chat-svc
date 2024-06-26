package io.github.onecx.chat.rs.internal.mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ValueMapping;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gen.io.github.onecx.ai.clients.model.ChatMessage;
import gen.io.github.onecx.ai.clients.model.ChatMessage.TypeEnum;
import gen.io.github.onecx.ai.clients.model.Conversation;
import gen.io.github.onecx.chat.rs.internal.model.*;
import io.github.onecx.chat.domain.criteria.ChatSearchCriteria;
import io.github.onecx.chat.domain.models.Chat;
import io.github.onecx.chat.domain.models.Message;
import io.github.onecx.chat.domain.models.Message.MessageType;
import io.github.onecx.chat.domain.models.Participant;
import io.github.onecx.chat.domain.models.Participant.ParticipantType;

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
    //@Mapping(target = "participants", source = "participants")
    @Mapping(target = "participants", ignore = true)
    public abstract Chat create(CreateChatDTO object);

    public List<Participant> mapParticipantDTOs(List<ParticipantDTO> participantDTOs) {

        List<Participant> participants = new ArrayList<>();

        if (participantDTOs != null) {
            for (ParticipantDTO participantDTO : participantDTOs) {
                Participant participant = mapParticipant(participantDTO);
                if (participant.getId() == null) {
                    participant.setId(UUID.randomUUID().toString());
                }
                participants.add(participant);
            }
        }

        return participants;

    }

    public Set<Participant> mapParticipants(List<ParticipantDTO> participantDTOs) {
        Set<Participant> participants = new HashSet<>();

        if (participantDTOs != null && !participantDTOs.isEmpty()) {
            for (ParticipantDTO participantDTO : participantDTOs) {

                Participant participant = new Participant();

                if (participantDTO.getId() != null) {
                    participant.setId(participantDTO.getId());
                } else {
                    participant.setId(UUID.randomUUID().toString());
                }
                participant.setUserName(participantDTO.getUserName());
                participant.setUserId(participantDTO.getUserId());
                participant.setEmail(participantDTO.getEmail());
                participant.setType(ParticipantType.valueOf(participantDTO.getType().name()));

                participants.add(participant);
            }
        }

        return participants;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "chat", ignore = true)
    public abstract Message createMessage(CreateMessageDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "chat", ignore = true)
    public abstract Participant addParticipant(AddParticipantDTO dto);

    @IterableMapping(qualifiedByName = "mapSingleChat")
    public abstract List<ChatDTO> map(Stream<Chat> entity);

    @Mapping(target = "version", source = "modificationCount")
    @Mapping(target = "removeParticipantsItem", ignore = true)
    @Mapping(target = "participants", ignore = true)
    @Named("mapSingleChat")
    public abstract ChatDTO mapSingleChat(Chat chat);

    @Mapping(target = "version", source = "modificationCount")
    @Mapping(target = "removeParticipantsItem", ignore = true)
    public abstract ChatDTO mapChat(Chat chat);

    @Mapping(target = "version", source = "modificationCount")
    public abstract ParticipantDTO mapParticipant(Participant participant);

    public abstract List<ParticipantDTO> mapParticipantList(List<Participant> items);

    @Mapping(target = "version", source = "modificationCount")
    public abstract MessageDTO map(Message message);

    public abstract List<MessageDTO> mapMessageList(List<Message> items);

    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "chat", ignore = true)
    public abstract Participant mapParticipant(ParticipantDTO participantDTO);

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
    @Mapping(target = "participants", ignore = true)
    public abstract void update(UpdateChatDTO chatDTO, @MappingTarget Chat entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "messages", ignore = true)
    public abstract Chat map(UpdateChatDTO object);

    @Mapping(source = "id", target = "conversationId")
    @Mapping(source = "messages", target = "history")
    @Mapping(target = "conversationType", ignore = true)
    public abstract Conversation mapChat2Conversation(Chat object);

    @Mapping(source = "id", target = "conversationId")
    @Mapping(source = "text", target = "message")
    public abstract ChatMessage mapMessage(Message object);

    @ValueMapping(source = "HUMAN", target = "USER")
    public abstract TypeEnum mapMessage(MessageType object);

    public Long mapLocalDateTime(LocalDateTime time) {
        return time.toEpochSecond(ZoneOffset.UTC);
    }

    public LocalDateTime mapLongTime(Long timestamp) {
        return Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @ValueMapping(source = "USER", target = "HUMAN")
    @ValueMapping(source = "ACTION", target = MappingConstants.NULL)
    @Mapping(target = "controlTraceabilityManual", ignore = true)
    @Mapping(target = "modificationCount", ignore = true)
    @Mapping(target = "creationUser", constant = "AiSvc")
    @Mapping(target = "modificationUser", constant = "AiSvc")
    @Mapping(target = "persisted", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "modificationDate", ignore = true)
    @Mapping(target = "chat", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "text", source = "message")
    @Mapping(target = "userName", ignore = true)
    public abstract Message mapAiSvcMessage(ChatMessage chatResponse);

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
