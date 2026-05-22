package it.polimi.ingsw.am56.controller;

import it.polimi.ingsw.am56.database.DatabaseManager;
import it.polimi.ingsw.am56.database.LeaderboardDAO;
import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.GameObserver;
import it.polimi.ingsw.am56.model.state.GameEvent;
import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;

import java.time.LocalDate;
import java.util.List;

public class GameResultSaver implements GameObserver {

    private final GameModel model;
    private final LeaderboardDAO db;

    public GameResultSaver(GameModel model) {
        this.model = model;
        this.db = new DatabaseManager();
    }

    @Override
    public void onModelChanged(GameEvent event) {
        if (event == GameEvent.GAME_ENDED) {
            List<EndGameDTO> results = model.getPlayers().stream()
                    .map(p -> new EndGameDTO(0, p.getNickname(), p.getStats().getPoints(),
                            p.getStats().getFood(), false, LocalDate.now().toString()))
                    .toList();
            db.saveMatchResults(model.getGameSize(), results, LocalDate.now());
        }
    }
}