package com.tugatobrasa.api.translation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class TranslationControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("tugatobrasa")
            .withUsername("tugatobrasa")
            .withPassword("tugatobrasa");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void translatesExactMatch() throws Exception {
        mockMvc.perform(post("/api/v1/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"autocarro","direction":"PT_TO_BR"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullTranslation").value("ônibus"))
                .andExpect(jsonPath("$.translations[0].falseFriend").value(false))
                .andExpect(jsonPath("$.status").doesNotExist());
    }

    @Test
    void flagsFalseFriendAsWarning() throws Exception {
        mockMvc.perform(post("/api/v1/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"bicha","direction":"PT_TO_BR"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.translations[0].falseFriend").value(true))
                .andExpect(jsonPath("$.warnings[0]").isNotEmpty());
    }

    @Test
    void resolvesOppositeDirection() throws Exception {
        mockMvc.perform(post("/api/v1/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"propina","direction":"BR_TO_PT"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullTranslation").value("suborno"));
    }

    @Test
    void returnsNotFoundStatusWithoutHttpError() throws Exception {
        mockMvc.perform(post("/api/v1/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"termoquenaoexiste","direction":"PT_TO_BR"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.translations").isEmpty());
    }

    @Test
    void rejectsInvalidRequestWithProblemDetails() throws Exception {
        mockMvc.perform(post("/api/v1/translate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"autocarro"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
