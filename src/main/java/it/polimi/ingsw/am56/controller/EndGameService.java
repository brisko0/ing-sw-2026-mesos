package it.polimi.ingsw.am56.controller;

import it.polimi.ingsw.am56.database.LeaderboardDAO;
import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.GameObserver;
import it.polimi.ingsw.am56.model.state.GameEvent;
import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;
import it.polimi.ingsw.am56.network.message.update.GameEndedMessage;

import java.time.LocalDate;
import java.util.List;

public class EndGameService implements GameObserver {

    private final GameModel model;
    private final LeaderboardDAO dao;
    private volatile GameEndedMessage cachedMessage;

    public EndGameService(GameModel model, LeaderboardDAO dao) {
        this.model = model;
        this.dao = dao;
    }

    @Override
    public void onModelChanged(GameEvent event) {
        if (event != GameEvent.GAME_ENDED) return;

        List<EndGameDTO> matchRanking = computeMatchRanking();
        dao.saveMatchResults(model.getGameSize(), matchRanking, LocalDate.now());
        List<EndGameDTO> globalLeaderboard = dao.getGlobalLeaderboard(model.getGameSize());

        this.cachedMessage = new GameEndedMessage(
                matchRanking, globalLeaderboard, model.getGameSize());
    }

    public GameEndedMessage getEndGameMessage() {
        return cachedMessage;
    }

    private List<EndGameDTO> computeMatchRanking() {
        String today = LocalDate.now().toString();
        return model.computeRanking().stream()
                .map(r -> new EndGameDTO(
                        r.rank(),
                        r.player().getNickname(),
                        r.player().getStats().getPoints(),
                        r.player().getStats().getFood(),
                        r.isWinner(),
                        today))
                .toList();
    }
}