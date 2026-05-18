package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.EndGameDTO;
import java.util.List;

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