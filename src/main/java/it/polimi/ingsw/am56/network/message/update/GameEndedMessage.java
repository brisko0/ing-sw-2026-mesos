package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;
import java.util.List;

/**
 * Sent when the game ends, carrying both the final standings of the just-played match and the
 * persisted global leaderboard for games of the same size, so clients can show the results and
 * the all-time ranking together.
 *
 * @param matchRanking      final standings of the just-played match
 * @param globalLeaderboard all-time leaderboard for games of this size
 * @param gameSize          the number of players the game was played with
 */
public record GameEndedMessage(
        List<EndGameDTO> matchRanking,
        List<EndGameDTO> globalLeaderboard,
        int gameSize
) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}