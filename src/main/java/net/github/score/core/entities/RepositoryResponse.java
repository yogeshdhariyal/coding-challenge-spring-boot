package net.github.score.core.entities;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RepositoryResponse {

    private String name;
    private int stars;
    private int forks;
    private Instant lastUpdatedAt;
    private double score;
    private String language;
}
