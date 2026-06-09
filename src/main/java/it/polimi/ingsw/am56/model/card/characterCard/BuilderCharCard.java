package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;

import java.io.Serial;


/**
 * Builder character card. On pick, adds a per-building food discount and contributes to the
 * builder-points pool used at end-of-game scoring.
 */
public class BuilderCharCard extends CharacterCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int buildingSale;
    private final int builderPoints;

    /**
     * Creates a builder character card.
     *
     * @param cardId        unique card id
     * @param era           era the card belongs to (1, 2 or 3)
     * @param minPlayers    minimum player count required for this card to be in the deck
     * @param buildingSale  food discount granted on every subsequent building purchase
     * @param builderPoints builder-points contribution toward end-of-game scoring
     */
    public BuilderCharCard(int cardId, int era, int minPlayers, int buildingSale, int builderPoints) {
        super( cardId, era, minPlayers);
        this.buildingSale = buildingSale;
        this.builderPoints = builderPoints;
    }

    /**
     * Always returns {@link CharacterType#BUILDER}.
     *
     * @return {@link CharacterType#BUILDER}
     */
    @Override
    public CharacterType getCharacterType() {
        return CharacterType.BUILDER;
    }

    /**
     * Adds {@code buildingSale} to the current player's building-discount counter and
     * {@code builderPoints} to their builder-points counter.
     *
     * @param gameModel current game model
     */
    @Override
    public void resolveEffect(GameModel gameModel){

        gameModel.getCurrentPlayer().getStats().addBuildingSale(buildingSale);
        gameModel.getCurrentPlayer().getStats().addBuilderPoints(builderPoints);

    }
}
