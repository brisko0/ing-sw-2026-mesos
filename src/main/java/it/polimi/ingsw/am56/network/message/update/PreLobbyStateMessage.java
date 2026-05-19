package it.polimi.ingsw.am56.network.message.update;

import java.util.List;

public record PreLobbyStateMessage(
        boolean isGameAlreadyCreated,
        boolean isGameInProgress,
        List<String> takenNicknames,
        List<String> takenColors
) implements ServerMessage {

    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}