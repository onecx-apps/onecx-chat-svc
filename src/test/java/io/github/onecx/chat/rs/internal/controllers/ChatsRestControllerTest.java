package io.github.onecx.chat.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tkit.quarkus.test.WithDBData;

import gen.io.github.onecx.chat.rs.internal.model.*;
import io.github.onecx.chat.test.AbstractTest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(ChatsRestController.class)
@WithDBData(value = "data/testdata-internal.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
class ChatsRestControllerTest extends AbstractTest {

    @Test
    void deleteChatTest() {

        // delete chat
        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "chat-DELETE_1")
                .delete("{id}")
                .then().statusCode(NO_CONTENT.getStatusCode());

        // check if chat exists
        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "chat-DELETE_1")
                .get("{id}")
                .then().statusCode(NOT_FOUND.getStatusCode());

        // delete chat in portal
        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "chat-11-111")
                .delete("{id}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

    }

    @Test
    void getChatByIdTest() {

        var dto = given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "chat-22-222")
                .get("{id}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(ChatDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getType()).isEqualTo(ChatTypeDTO.AI_CHAT);
        assertThat(dto.getId()).isEqualTo("chat-22-222");

        given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "___")
                .get("{id}")
                .then().statusCode(NOT_FOUND.getStatusCode());

        dto = given()
                .contentType(APPLICATION_JSON)
                .pathParam("id", "chat-11-111")
                .get("{id}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(ChatDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getType()).isEqualTo(ChatTypeDTO.HUMAN_CHAT);
        assertThat(dto.getId()).isEqualTo("chat-11-111");

    }

    @Test
    void getChatsTest() {
        var data = given()
                .contentType(APPLICATION_JSON)
                .get()
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(ChatPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(3);
        assertThat(data.getStream()).isNotNull().hasSize(3);

    }

    @Test
    void searchChatsTest() {
        var criteria = new ChatSearchCriteriaDTO();

        var data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(ChatPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(3);
        assertThat(data.getStream()).isNotNull().hasSize(3);

        criteria.setType(null);
        data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(ChatPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(3);
        assertThat(data.getStream()).isNotNull().hasSize(3);

        criteria.setType(ChatTypeDTO.HUMAN_CHAT);
        data = given()
                .contentType(APPLICATION_JSON)
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(ChatPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(1);
        assertThat(data.getStream()).isNotNull().hasSize(1);

        criteria.setType(null);
        criteria.setCreationUser("user1");
        data = given()
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("org1"))
                .body(criteria)
                .post("/search")
                .then()
                .statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .as(ChatPageResultDTO.class);

        assertThat(data).isNotNull();
        assertThat(data.getTotalElements()).isEqualTo(2);
        assertThat(data.getStream()).isNotNull().hasSize(2);

    }

    @Test
    void updateChatTest() {

        // update none existing chat
        var chatDto = new UpdateChatDTO();
        chatDto.setType(ChatTypeDTO.HUMAN_CHAT);
        chatDto.setTopic("topic-update");

        given()
                .contentType(APPLICATION_JSON)
                .body(chatDto)
                .when()
                .pathParam("id", "does-not-exists")
                .put("{id}")
                .then().statusCode(NOT_FOUND.getStatusCode());

        // update chat
        given()
                .contentType(APPLICATION_JSON)
                .body(chatDto)
                .when()
                .pathParam("id", "chat-11-111")
                .put("{id}")
                .then().statusCode(NO_CONTENT.getStatusCode());

        // download chat
        var dto = given().contentType(APPLICATION_JSON)
                .body(chatDto)
                .when()
                .pathParam("id", "chat-11-111")
                .get("{id}")
                .then().statusCode(OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract()
                .body().as(ChatDTO.class);

        assertThat(dto).isNotNull();
        assertThat(dto.getTopic()).isEqualTo(chatDto.getTopic());

    }

    @Test
    void updateChatWithoutBodyTest() {

        var exception = given()
                .contentType(APPLICATION_JSON)
                .when()
                .pathParam("id", "update_create_new")
                .put("{id}")
                .then()
                .statusCode(BAD_REQUEST.getStatusCode())
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(exception);
        Assertions.assertEquals("CONSTRAINT_VIOLATIONS", exception.getErrorCode());
        Assertions.assertEquals("updateChat.updateChatDTO: must not be null",
                exception.getDetail());
        Assertions.assertNotNull(exception.getInvalidParams());
        Assertions.assertEquals(1, exception.getInvalidParams().size());
    }
}
