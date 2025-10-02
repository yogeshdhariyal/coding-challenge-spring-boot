package net.github.score.entrypoints.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.github.score.core.entities.RepositoryResponse;
import net.github.score.core.usecases.score.GetScores;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/repositories")
@RequiredArgsConstructor
public class RepositoriesController {

    private final GetScores getScores;

    @Operation(summary = "Get GitHub repositories with scores", description = "Returns repositories scored by stars, forks, and last update.", responses = {
        @ApiResponse(responseCode = "200", description = "Successful response", content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error")})
    @GetMapping("/scores")
    public List<RepositoryResponse> getRepositoryScores(
        @Parameter(description = "Language to filter (default- Java)")
        @RequestParam(value = "language", required = false, defaultValue = "Java") String language,
        @Parameter(description = "Created after date in yyyy-MM-dd format (default- 1 month ago)")
        @RequestParam(value = "createdAfter", required = false) String createdAfter) {

        getScores.execute(language, createdAfter);
        return getScores.getResult();
    }
}