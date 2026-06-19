package it.polimi.ingsw.am56.network.message.update;

import java.util.List;

/**
 * Sent in response to a pre-lobby state sync, telling a connecting client whether a game already
 * exists and is in progress and which nicknames and colours are already taken, so the client can
 * decide whether to create / join / reconnect and validate the player's choices.
 *
 * @param isGameAlreadyCreated whether a game has already been created
 * @param isGameInProgress     whether the created game has already started (i.e. is past the
 *                             players-filling phase)
 * @param takenNicknames       the nicknames already in use
 * @param takenColors          the totem colours already in use
 */
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