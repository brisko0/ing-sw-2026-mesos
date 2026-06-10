package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;

import java.io.Serial;

/**
 * Building card that, at the end of the game, awards the owner {@code pointsPerCharacter}
 * victory points per character of a specific {@link CharacterType} they own.
 */
public class PointsPerCharType extends BuildingCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final CharacterType characterType;
    private final int pointsPerCharacter;

    /**
     * Creates a points-per-character-type building card.
     *
     * @param cardId             unique card id
     * @param era                era the card belongs to (1, 2 or 3)
     * @param numPlayers         minimum player count required for this card to be in the deck
     * @param cost               food cost to acquire the building (before the building-sale discount)
     * @param points             base victory points granted at the end of the game
     * @param characterType      the character archetype the bonus is keyed to
     * @param pointsPerCharacter victory points granted per character of {@code characterType}
     */
    public PointsPerCharType(int cardId, int era, int numPlayers, int cost, int points, CharacterType characterType, int pointsPerCharacter) {
        super(cardId, era, numPlayers, cost, points);
        this.characterType = characterType;
        this.pointsPerCharacter = pointsPerCharacter;
    }

    /**
     * Grants the base building points (via {@code super}) and adds
     * {@code pointsPerCharacter} per character of {@code characterType} owned.
     *
     * @param gameModel current game model
     * @param player    the owner of this building card
     */
    @Override
    public void onGameEnding(GameModel gameModel, Player player) {
        super.onGameEnding(gameModel, player);
        player.getStats().addPoints(pointsPerCharacter * player.getStats().getCharacterTypeNum(characterType));
    }
}
