package it.polimi.ingsw.am56.controller;

import it.polimi.ingsw.am56.database.LeaderboardDAO;
import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.GameObserver;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.state.GameEvent;
import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;
import it.polimi.ingsw.am56.network.message.update.GameEndedMessage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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
        List<Player> sorted = new ArrayList<>(model.getPlayers());
        sorted.sort(Comparator
                .comparingInt((Player p) -> p.getStats().getPoints()).reversed()
                .thenComparing(Comparator
                        .comparingInt((Player p) -> p.getStats().getFood()).reversed()));

        int highestScore = sorted.isEmpty() ? 0 : sorted.getFirst().getStats().getPoints();
        int winnerFood   = sorted.isEmpty() ? 0 : sorted.getFirst().getStats().getFood();
        String today = LocalDate.now().toString();

        List<EndGameDTO> result = new ArrayList<>();
        int rank = 1;
        for (Player p : sorted) {
            boolean isWinner = p.getStats().getPoints() == highestScore
                    && p.getStats().getFood() == winnerFood;
            result.add(new EndGameDTO(rank++, p.getNickname(),
                    p.getStats().getPoints(), p.getStats().getFood(), isWinner, today));
        }
        return result;
    }
}