package it.polimi.ingsw.am56.database;

import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Persistence abstraction for the cross-game leaderboard. It decouples the end-game logic from the
 * concrete storage, so the results of a finished match can be saved and the all-time leaderboard
 * read back without callers knowing how (or whether) it is backed by a database.
 */
public interface LeaderboardDAO {
    /**
     * Persists the results of one finished match.
     *
     * @param gameSize the number of players the match was played with (leaderboards are kept per size)
     * @param results  the per-player results to store
     * @param date     the date the match was played
     */
    void saveMatchResults(int gameSize, List<EndGameDTO> results, LocalDate date);

    /**
     * Returns the all-time leaderboard for games of the given size, ordered from best to worst and
     * ranked accordingly.
     *
     * @param gameSize the player count whose leaderboard to read
     * @return the ranked leaderboard entries (empty if none or on failure)
     */
    List<EndGameDTO> getGlobalLeaderboard(int gameSize);
}