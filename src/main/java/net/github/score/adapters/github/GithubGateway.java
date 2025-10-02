package net.github.score.adapters.github;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.github.score.core.boundaries.RepositoryGateway;
import net.github.score.entrypoints.web.handler.GenericException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GithubGateway implements RepositoryGateway {

    @Value("${github.url}")
    private String githubUrl;

    @Value("${github.token}")
    private String githubToken;

    @Value("${github.default.per-page}")
    private String defaultPageSize;

    @Override
    public String searchRepositories(String language, String createdAfter) {

        HttpResponse<JsonNode> response = Unirest.get(githubUrl)
            .queryString("q", String.format("language:%s created:>%s", language, createdAfter))
            .queryString("per_page", defaultPageSize)
            .header("Authorization", "Bearer " + githubToken)
            .header("Accept", "application/vnd.github+json")
            .asJson();

        if (response.getStatus() != 200) {
            log.error("GitHub API error: {} - {}", response.getStatus(), response.getStatusText());
            throw new GenericException("GitHub API error: " + response.getStatusText());
        }

        return response.getBody().toString();
    }
}
