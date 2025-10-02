package net.github.score.adapters.github;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import net.github.score.entrypoints.web.handler.GenericException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class GithubGatewayIntegrationTest {

    private GithubGateway githubGateway;
    private MockWebServer mockWebServer;

    @BeforeEach
    void setup() throws IOException {
        githubGateway = new GithubGateway();
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        ReflectionTestUtils.setField(githubGateway, "githubUrl", mockWebServer.url("/").toString());
        ReflectionTestUtils.setField(githubGateway, "githubToken", "fake-token");
        ReflectionTestUtils.setField(githubGateway, "defaultPageSize", "5");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testSearchRepositoriesSuccess() throws JsonProcessingException {
        String json = """
                {
                  "items": [
                    {
                      "name": "repo1",
                      "stars": 10,
                      "forks": 5,
                      "updatedAt": "2025-10-01T12:00:00Z",
                      "language": "Java"
                    },
                    {
                      "name": "repo2",
                      "stars": 20,
                      "forks": 10,
                      "updatedAt": "2025-09-15T08:30:00Z",
                      "language": "Java"
                    }
                  ]
                }
            """;

        mockWebServer.enqueue(new MockResponse()
                                  .setBody(json)
                                  .setHeader("Content-Type", "application/json")
                                  .setResponseCode(200));

        String response = githubGateway.searchRepositories("Java", "2025-09-01");

        assertNotNull(response);

        JsonNode root = new ObjectMapper().readTree(response);
        JsonNode items = root.get("items");

        assertNotNull(items);
        assertEquals(2, items.size());

        assertEquals("repo1", items.get(0).get("name").asText());
        assertEquals(10, items.get(0).get("stars").asInt());
        assertEquals(5, items.get(0).get("forks").asInt());
        assertEquals("Java", items.get(0).get("language").asText());

        assertEquals("repo2", items.get(1).get("name").asText());
        assertEquals(20, items.get(1).get("stars").asInt());
        assertEquals(10, items.get(1).get("forks").asInt());
        assertEquals("Java", items.get(1).get("language").asText());
    }

    @Test
    void testSearchRepositoriesFailure() {
        mockWebServer.enqueue(new MockResponse()
                                  .setBody("Internal Server Error")
                                  .setResponseCode(500));

        GenericException ex = assertThrows(GenericException.class,
                                           () -> githubGateway.searchRepositories("Python", "2025-01-01"));
        assertTrue(ex.getMessage().contains("GitHub API error"));
    }
}
