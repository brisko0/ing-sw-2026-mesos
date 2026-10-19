package it.polimi.ingsw.am56.network.message.update;

import it.polimi.ingsw.am56.network.message.classesDTO.EventOutcomeDTO;

import java.util.List;

/**
 * Notifies clients that an Event card has been resolved, carrying its id and the per-player
 * Food/Points outcome ({@link EventOutcomeDTO}) so the events log can show what each player
 * gained or lost.
 */
public record EventResolvedMessage(int cardId, List<EventOutcomeDTO> outcomes) implements ServerMessage {
    @Override
    public void accept(MessageVisitor visitor) {
        visitor.visit(this);
    }
}
