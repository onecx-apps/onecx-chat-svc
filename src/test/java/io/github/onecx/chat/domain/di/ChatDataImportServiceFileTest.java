package io.github.onecx.chat.domain.di;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.onecx.chat.domain.daos.ChatDAO;
import io.github.onecx.chat.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@DisplayName("Chat data import test from example file")
@TestProfile(ChatDataImportServiceFileTest.CustomProfile.class)
class ChatDataImportServiceFileTest extends AbstractTest {

    @Inject
    ChatDAO dao;

    @Test
    @DisplayName("Import chat data from file")
    void importDataFromFileTest() {
        var data = dao.findAll().toList();
        assertThat(data).isNotNull().hasSize(2);
    }

    public static class CustomProfile implements QuarkusTestProfile {

        @Override
        public String getConfigProfile() {
            return "test";
        }

        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "tkit.dataimport.enabled", "true",
                    "tkit.dataimport.configurations.chat.enabled", "true",
                    "tkit.dataimport.configurations.chat.file", "./src/test/resources/import/chat-import.json",
                    "tkit.dataimport.configurations.chat.metadata.operation", "CLEAN_INSERT",
                    "tkit.dataimport.configurations.chat.stop-at-error", "true");
        }
    }

}
