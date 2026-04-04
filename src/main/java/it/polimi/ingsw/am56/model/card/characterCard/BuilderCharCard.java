package it.polimi.ingsw.am56.model.card.characterCard;

import it.polimi.ingsw.am56.model.GameModel;

public class BuilderCharCard extends CharacterCard {
    private final int buildingSale;
    private final int builderPoints;

    public BuilderCharCard(int cardId, int era, int minPlayers, int buildingSale, int builderPoints) {
        super( cardId, era, minPlayers);
        this.buildingSale = buildingSale;
        this.builderPoints = builderPoints;
    }

    @Override
    public void resolveEffect(GameModel gameModel){

        gameModel.getCurrentPlayer().getStats().addBuildingSale(buildingSale);
        gameModel.getCurrentPlayer().getStats().addBuilderPoints(builderPoints);
    }

    @Override
    public CharacterType getCharacterType(){
        return CharacterType.BUILDER;
    }
}