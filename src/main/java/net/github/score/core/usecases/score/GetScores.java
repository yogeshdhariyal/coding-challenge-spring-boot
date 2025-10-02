package net.github.score.core.usecases.score;

import static net.github.score.core.usecases.util.ScoringAlgorithm.calculateScore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.github.score.adapters.github.GitHubRepositoryItem;
import net.github.score.adapters.github.GitHubSearchResponse;
import net.github.score.core.boundaries.RepositoryGateway;
import net.github.score.core.entities.RepositoryResponse;
import net.github.score.entrypoints.web.handler.GenericException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetScores {

    private final ObjectMapper objectMapper;
    private final RepositoryGateway repositoryGateway;

    @Value("${github.default.language}")
    private String defaultLanguage;

    @Getter
    private List<RepositoryResponse> result;

    public void execute(String language, String repositoryCreatedAfter) {
        if (language == null || language.isBlank()) {
            language = defaultLanguage;
        }

        // Determine the date for filtering repositories
        String createdAfterDateISO = getRepositoryCreatedAfterDate(repositoryCreatedAfter);

        log.info("🔍 Searching repositories (language={} createdAfter={})",
                 language, createdAfterDateISO);

        // Fetch data from repository eg. GitHub
        String response = repositoryGateway.searchRepositories(language, createdAfterDateISO);

        try {
            GitHubSearchResponse searchResponse =
                objectMapper.readValue(response, GitHubSearchResponse.class);
            if (searchResponse.getItems() == null || searchResponse.getItems().isEmpty()) {
                log.info("No repositories found for the given criteria.");
                return;
            }
            result = searchResponse.getItems().stream().map(this::mapToResponse).toList();
        } catch (JsonProcessingException e) {
            throw new GenericException("Parsing error check logs for details", e);
        }
    }

    private String getRepositoryCreatedAfterDate(String repositoryCreatedAfter) {
        LocalDate repositoryCreatedAfterDate;
        if (repositoryCreatedAfter == null || repositoryCreatedAfter.isBlank()) {
            repositoryCreatedAfterDate = LocalDate.now().minusMonths(1);
        } else {
            try {
                repositoryCreatedAfterDate = LocalDate.parse(repositoryCreatedAfter);
            } catch (DateTimeParseException e) {
                log.error("Invalid date format. Use ISO yyyy-MM-dd (e.g., 2025-10-01).");
                throw new GenericException("Invalid date format. Use ISO yyyy-MM-dd (e.g., 2025-10-01).");
            }
        }
        return repositoryCreatedAfterDate.format(DateTimeFormatter.ISO_DATE);
    }

    private RepositoryResponse mapToResponse(GitHubRepositoryItem item) {
        Instant updatedAt = Instant.parse(item.getUpdatedAt());
        return new RepositoryResponse().setName(item.getName())
            .setStars(item.getStars())
            .setForks(item.getForks())
            .setLastUpdatedAt(updatedAt)
            .setLanguage(item.getLanguage())
            .setScore(calculateScore(item.getStars(), item.getForks(), updatedAt));
    }
}
