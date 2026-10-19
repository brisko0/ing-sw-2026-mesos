package it.polimi.ingsw.am56.model;

/**
 * One row of the final ranking produced by {@link GameModel#computeRanking()}: the player,
 * their 1-based {@code rank} (shared by players tied on points <em>and</em> food), and whether
 * they are among the winners (shared victory is possible). This is a domain-level result —
 * mapping it to any transport/DTO form is the caller's responsibility, so the model stays
 * independent of the network and persistence layers.
 *
 * @param rank     1-based final position; equal for players tied on (points, food)
 * @param player   the ranked player
 * @param isWinner {@code true} if the player ties for both the highest score and the most food
 */
public record RankingEntry(int rank, Player player, boolean isWinner) {}
