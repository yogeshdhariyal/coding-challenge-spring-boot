package net.github.score.core.usecases.util;

import java.time.Instant;

public class ScoringAlgorithm {

    public static double calculateScore(int stars, int forks, Instant updatedAt) {

        double w1 = 0.5, w2 = 0.3, w3 = 0.2;

        double starsPart = Math.log(1 + stars);
        double forksPart = Math.log(1 + forks);

        long daysSinceUpdate = java.time.Duration.between(updatedAt, Instant.now()).toDays();
        double freshness = 1.0 / (1 + daysSinceUpdate);

        return (w1 * starsPart) + (w2 * forksPart) + (w3 * freshness * 100);
    }
}
