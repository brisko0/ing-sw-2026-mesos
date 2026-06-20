package it.polimi.ingsw.am56.controller;

import it.polimi.ingsw.am56.database.LeaderboardDAO;
import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.GameObserver;
import it.polimi.ingsw.am56.model.state.GameEvent;
import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;
import it.polimi.ingsw.am56.network.message.update.GameEndedMessage;

import java.time.LocalDate;
import java.util.List;

/**
 * Server-side {@link GameObserver} that handles end-of-game processing. When the model signals
 * {@link GameEvent#GAME_ENDED}, it computes the final match ranking, persists it through the
 * {@link LeaderboardDAO}, reads back the global leaderboard and caches a {@link GameEndedMessage}.
 * The cached message is then served to the per-client virtual views so every player receives the same
 * final results.
 */
public class EndGameService implements GameObserver {

    private final GameModel model;
    private final LeaderboardDAO dao;
    private volatile GameEndedMessage cachedMessage;

    /**
     * Creates the service over the given model and leaderboard store.
     *
     * @param model the game model to observe
     * @param dao   the leaderboard persistence used to save results and read the global leaderboard
     */
    public EndGameService(GameModel model, LeaderboardDAO dao) {
        this.model = model;
        this.dao = dao;
    }

    /**
     * On {@link GameEvent#GAME_ENDED}, computes and persists the match ranking, fetches the global
     * leaderboard and caches the resulting end-game message; ignores all other events.
     *
     * @param event the event signalled by the model
     */
    @Override
    public void onModelChanged(GameEvent event) {
        if (event != GameEvent.GAME_ENDED) return;

        List<EndGameDTO> matchRanking = computeMatchRanking();
        dao.saveMatchResults(model.getGameSize(), matchRanking, LocalDate.now());
        List<EndGameDTO> globalLeaderboard = dao.getGlobalLeaderboard(model.getGameSize());

        this.cachedMessage = new GameEndedMessage(
                matchRanking, globalLeaderboard, model.getGameSize());
    }

    /**
     * @return the cached end-game message, or {@code null} if the game has not ended yet
     */
    public GameEndedMessage getEndGameMessage() {
        return cachedMessage;
    }

    /**
     * Maps the model's domain-level ranking ({@link GameModel#computeRanking()}) to the
     * {@link EndGameDTO} list consumed by the persistence layer and the end-game message. The
     * tie-break logic now lives in the model; here we only attach the match date and flatten
     * each entry's player into the transport record.
     */
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