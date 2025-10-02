package net.github.score.entrypoints.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import net.github.score.core.entities.RepositoryResponse;
import net.github.score.core.usecases.score.GetScores;
import net.github.score.entrypoints.web.handler.GenericException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RepositoriesController.class)
class RepositoriesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetScores getScores;

    @Test
    void should_ReturnRepositoryScoresWithDefaults() throws Exception {

        RepositoryResponse repo = new RepositoryResponse().setName("repo1")
            .setLanguage("Java")
            .setStars(100)
            .setForks(20)
            .setLastUpdatedAt(Instant.now());
        Mockito.when(getScores.getResult()).thenReturn(List.of(repo));

        mockMvc.perform(get("/repositories/scores")
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("repo1"))
            .andExpect(jsonPath("$[0].language").value("Java"))
            .andExpect(jsonPath("$[0].stars").value(100))
            .andExpect(jsonPath("$[0].forks").value(20));

        Mockito.verify(getScores).execute(eq("Java"), Mockito.isNull());
    }

    @Test
    void should_ReturnRepositoryScoresWithParams() throws Exception {
        RepositoryResponse repo = new RepositoryResponse().setName("repo2")
            .setLanguage("Python")
            .setStars(50)
            .setForks(10)
            .setLastUpdatedAt(Instant.now());
        Mockito.when(getScores.getResult()).thenReturn(List.of(repo));

        mockMvc.perform(get("/repositories/scores")
                            .param("language", "Python")
                            .param("createdAfter", "2025-01-01")
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("repo2"))
            .andExpect(jsonPath("$[0].language").value("Python"))
            .andExpect(jsonPath("$[0].stars").value(50))
            .andExpect(jsonPath("$[0].forks").value(10));

        Mockito.verify(getScores).execute(eq("Python"), eq("2025-01-01"));
    }

    @Test
    void should_Return500_WhenUseCaseFails() throws Exception {
        Mockito.doThrow(new GenericException("Unexpected error"))
            .when(getScores).execute(Mockito.anyString(), Mockito.any());

        mockMvc.perform(get("/repositories/scores")
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
    }
}
