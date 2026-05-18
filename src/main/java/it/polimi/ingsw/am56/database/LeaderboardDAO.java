package it.polimi.ingsw.am56.database;

import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;
import java.util.List;

public interface LeaderboardDAO {
    void saveMatchResults(int gameSize, List<EndGameDTO> results);

    List<EndGameDTO> getGlobalLeaderboard(int gameSize);
}