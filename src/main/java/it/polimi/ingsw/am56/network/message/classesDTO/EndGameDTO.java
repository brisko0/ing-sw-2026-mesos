package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;

/**
 * A single standings row produced at the end of a game. The same record is reused both for the
 * just-played match ranking and for the persisted global leaderboard, and is the unit stored by
 * and read back from the leaderboard database.
 *
 * @param rank     finishing position (1 = first); ties are already broken upstream by the model
 * @param nickname the player's nickname
 * @param score    the player's final victory points
 * @param food     the player's leftover food (used as the score tie-breaker)
 * @param isWinner whether this player won the match
 * @param date     the match date, as an ISO {@code yyyy-MM-dd} string
 */
public record EndGameDTO (
        int rank,
        String nickname,
        int score,
        int food,
        boolean isWinner,
        String date
) implements Serializable {}