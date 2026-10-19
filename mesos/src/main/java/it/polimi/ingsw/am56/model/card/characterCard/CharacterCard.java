package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;

import java.io.Serial;

/**
 * Base class for the six character cards. A character card is picked by a player during
 * their turn and remains in their hand for the rest of the game; its
 * {@link #resolveEffect(GameModel)} applies a per-type bonus at pick time, and its presence
 * may also contribute to end-of-game scoring and to event resolution.
 */
public abstract class CharacterCard extends Card {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a character card.
     *
     * @param cardId     unique card id
     * @param era        era the card belongs to (1, 2 or 3)
     * @param minPlayers minimum player count required for this card to be in the deck
     */
    public CharacterCard(int cardId, int era, int minPlayers) {
        super( cardId, era, minPlayers);
    }

    /**
     * Returns the archetype of this card, used both by the view layer for rendering and by
     * gameplay code that counts characters by type.
     *
     * @return the character type
     */
    public abstract CharacterType getCharacterType();

    /**
     * Notifies every building owned by the current player that they have just picked this
     * character card, so each building can apply its per-pick bonus.
     *
     * @param gameModel current game model
     */
    @Override
    public void resolveBuildingsEffects(GameModel gameModel) {
        for(BuildingCard buildingCard : gameModel.getCurrentPlayer().getStats().getBuildingCards()) {
            buildingCard.onCardPicked(gameModel, this);
        }
    }

    /**
     * Appends this card to the given player's character-cards collection. Called by the
     * state machine when a pick is confirmed.
     *
     * @param player the player who picked the card
     */
    @Override
    public void addCardToPlayer(Player player){
        player.getStats().getCharacterCards().add(this);
    }

    /**
     * Always returns {@link CardType#CHARACTER}.
     *
     * @return {@link CardType#CHARACTER}
     */
    @Override
    public CardType getCardType() {
        return CardType.CHARACTER;
    }

}
