package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.PlayerDTO;

/**
 * Sent when a player joins the lobby, carrying the players currently in it and the target game
 * size so clients can show how many slots are still open.
 *
 * @param players  the players currently in the lobby
 * @param gameSize the number of players the game was created for
 */
public record LobbyStateMessage(java.util.List<PlayerDTO> players, int gameSize) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
