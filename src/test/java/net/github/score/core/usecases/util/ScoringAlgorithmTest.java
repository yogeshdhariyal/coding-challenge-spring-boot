package net.github.score.core.usecases.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ScoringAlgorithmTest {

    @ParameterizedTest(name = "Stars={0}, Forks={1}")
    @CsvSource({
        "10, 5",
        "50, 5",
        "100, 5"
    })
    void scoreShouldIncrease_withStars(int stars, int forks) {
        Instant now = Instant.now();
        double scoreLow = ScoringAlgorithm.calculateScore(stars, forks, now);
        double scoreHigh = ScoringAlgorithm.calculateScore(stars * 2, forks, now);

        assertTrue(scoreHigh > scoreLow, "More stars should increase score");
    }

    @ParameterizedTest(name = "Stars={0}, Forks={1}")
    @CsvSource({
        "50, 2",
        "50, 20",
        "50, 50"
    })
    void scoreShouldIncrease_withForks(int stars, int forks) {
        Instant now = Instant.now();
        double scoreLow = ScoringAlgorithm.calculateScore(stars, forks, now);
        double scoreHigh = ScoringAlgorithm.calculateScore(stars, forks * 2, now);

        assertTrue(scoreHigh > scoreLow, "More forks should increase score");
    }

    @ParameterizedTest(name = "DaysAgo={0}")
    @MethodSource("updateDates")
    void scoreShouldBeHigherForRecentlyUpdatedRepo(long daysAgo) {
        Instant now = Instant.now();
        Instant oldDate = now.minus(daysAgo, ChronoUnit.DAYS);

        double recentScore = ScoringAlgorithm.calculateScore(50, 20, now);
        double oldScore = ScoringAlgorithm.calculateScore(50, 20, oldDate);

        assertTrue(recentScore > oldScore,
                   "Recently updated repo should have a higher score than one updated " + daysAgo + " days ago");
    }

    @Test
    void veryOldRepoShouldStillHaveNonNegativeScore() {
        Instant tenYearsAgo = Instant.now().minus(3650, ChronoUnit.DAYS);

        double score = ScoringAlgorithm.calculateScore(100, 50, tenYearsAgo);

        assertTrue(score >= 0, "Score should not be negative");
    }

    private static Stream<Long> updateDates() {
        return Stream.of(30L, 180L, 365L);
    }
}
