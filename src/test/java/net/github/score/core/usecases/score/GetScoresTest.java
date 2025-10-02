package net.github.score.core.usecases.score;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.github.score.adapters.github.GitHubRepositoryItem;
import net.github.score.adapters.github.GitHubSearchResponse;
import net.github.score.core.boundaries.RepositoryGateway;
import net.github.score.core.entities.RepositoryResponse;
import net.github.score.entrypoints.web.handler.GenericException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GetScoresTest {

    private ObjectMapper objectMapper;
    private RepositoryGateway repositoryGateway;
    private GetScores getScores;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        repositoryGateway = mock(RepositoryGateway.class);
        getScores = new GetScores(objectMapper, repositoryGateway);

        Field f = GetScores.class.getDeclaredField("defaultLanguage");
        f.setAccessible(true);
        f.set(getScores, "Java");
    }

    @ParameterizedTest(name = "language={0}, date={1}")
    @CsvSource({
        "Python, 2025-01-01",
        "Python, ''",
        "Python, invalid-date",
        "'', 2025-01-01",
        "'', ''",
        "null, 2025-01-01",
        "null, ''"
    })
    void should_HandleAllLanguageAndDateCombinations(String language, String date) throws Exception {

        if ("invalid-date".equals(date)) {
            assertThrows(GenericException.class, () -> getScores.execute(language, date));
        } else {
            GitHubSearchResponse mockResponse = new GitHubSearchResponse();
            mockResponse.setItems(List.of(buildItem()));

            String json = objectMapper.writeValueAsString(mockResponse);
            when(repositoryGateway.searchRepositories(anyString(), anyString()))
                .thenReturn(json);

            getScores.execute(language, date);

            String expectedLanguage = (language == null || language.isBlank()) ? "Java" : language;
            String expectedDate =
                (date == null || date.isBlank())
                ? LocalDate.now().minusMonths(1).format(DateTimeFormatter.ISO_DATE)
                : date;

            verify(repositoryGateway).searchRepositories(eq(expectedLanguage), eq(expectedDate));
        }
    }

    @Test
    void should_ReturnEmptyResult_whenNoItems() throws Exception {
        GitHubSearchResponse mockResponse = new GitHubSearchResponse();
        mockResponse.setItems(List.of());
        String json = objectMapper.writeValueAsString(mockResponse);

        when(repositoryGateway.searchRepositories(anyString(), anyString()))
            .thenReturn(json);

        getScores.execute("Java", "2025-01-01");

        assertNull(getScores.getResult());
    }

    @Test
    void should_MapRepositoryItemToResponse() throws Exception {
        GitHubRepositoryItem item = buildItem();

        GitHubSearchResponse mockResponse = new GitHubSearchResponse();
        mockResponse.setItems(List.of(item));

        String json = objectMapper.writeValueAsString(mockResponse);
        when(repositoryGateway.searchRepositories(anyString(), anyString()))
            .thenReturn(json);

        getScores.execute("Kotlin", "2025-01-01");

        List<RepositoryResponse> result = getScores.getResult();
        assertNotNull(result);
        assertEquals(1, result.size());

        RepositoryResponse repo = result.getFirst();
        assertEquals("test-repo", repo.getName());
        assertEquals("Kotlin", repo.getLanguage());
        assertEquals(50, repo.getStars());
        assertEquals(10, repo.getForks());
        assertTrue(repo.getScore() > 0);
    }

    @Test
    void shouldThrowGenericExceptionOnInvalidJson() {
        when(repositoryGateway.searchRepositories(anyString(), anyString()))
            .thenReturn("{invalid-json}");

        assertThrows(GenericException.class, () -> getScores.execute("Java", "2025-01-01"));
    }

    private static GitHubRepositoryItem buildItem() {
        GitHubRepositoryItem item = new GitHubRepositoryItem();
        item.setName("test-repo");
        item.setLanguage("Kotlin");
        item.setStars(50);
        item.setForks(10);
        item.setUpdatedAt(Instant.now().toString());
        return item;
    }
}
