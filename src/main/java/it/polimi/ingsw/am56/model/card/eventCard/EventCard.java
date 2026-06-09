package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;

import java.io.Serial;

/**
 * Base class for the four event cards. An event card is drawn at the end of each round and
 * applies a global effect to every player. Concrete subclasses implement
 * {@link #resolveEffect(GameModel)} with the rule-specific logic, and report their kind via
 * {@link #getEventType()}.
 */
public abstract class EventCard extends Card {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates an event card.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param minPlayers minimum player count required for this card to be in the deck
     */
    public EventCard(int cardId, int era, int minPlayers) {
        super(cardId, era, minPlayers);
    }

    /**
     * Returns the event kind, used by the view layer to render the right artwork and by
     * building cards to decide whether to react during {@link #resolveBuildingsEffects}.
     *
     * @return the event type
     */
    public abstract EvType getEventType();

    /**
     * Always returns {@link CardType#EVENT}.
     *
     * @return {@link CardType#EVENT}
     */
    @Override
    public CardType getCardType() {
        return CardType.EVENT;
    }

    /**
     * Notifies every player's building cards that this event is being resolved, so they can
     * apply their per-event bonuses. Called by the state machine after
     * {@link #resolveEffect(GameModel)}.
     *
     * @param gameModel current game model
     */
    @Override
    public void resolveBuildingsEffects(GameModel gameModel) {
        for(Player p : gameModel.getPlayers()){
            for(BuildingCard buildingCard : p.getStats().getBuildingCards()){
                buildingCard.onEventResolving(gameModel, this, p);
            }
        }
    }
}
