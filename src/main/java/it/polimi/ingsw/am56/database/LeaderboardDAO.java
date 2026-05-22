package it.polimi.ingsw.am56.database;

import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;

import java.time.LocalDate;
import java.util.List;

public interface LeaderboardDAO {
    void saveMatchResults(int gameSize, List<EndGameDTO> results, LocalDate date);

    List<EndGameDTO> getGlobalLeaderboard(int gameSize);
}