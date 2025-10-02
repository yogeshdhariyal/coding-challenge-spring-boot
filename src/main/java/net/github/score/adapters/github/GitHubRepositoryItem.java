package net.github.score.adapters.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubRepositoryItem {
    public String name;

    @JsonProperty("stargazers_count")
    public int stars;

    @JsonProperty("forks_count")
    public int forks;

    @JsonProperty("updated_at")
    public String updatedAt;

    @JsonProperty("language")
    public String language;
}
