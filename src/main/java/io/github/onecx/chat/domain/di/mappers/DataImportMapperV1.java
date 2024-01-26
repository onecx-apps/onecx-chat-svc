package io.github.onecx.chat.domain.di.mappers;

import jakarta.inject.Inject;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gen.io.github.onecx.chat.di.v1.model.DataImportChatDTOV1;
import io.github.onecx.chat.domain.models.Chat;

@Mapper(uses = OffsetDateTimeMapper.class)
public abstract class DataImportMapperV1 {

    @Inject
    ObjectMapper mapper;

    @Named("import")
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
    public abstract Chat importChat(DataImportChatDTOV1 dto);

    @Named("properties")
    String properties(Object properties) {
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
