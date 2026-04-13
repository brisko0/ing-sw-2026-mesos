package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;

public class PointsPerCharType extends BuildingCard {
    private final CharacterType characterType;
    private final int pointsPerCharacter;
    public PointsPerCharType(int cardId, int era, int numPlayers, int cost, int points, CharacterType characterType, int pointsPerCharacter) {
        super(cardId, era, numPlayers, cost, points);
        this.characterType = characterType;
        this.pointsPerCharacter = pointsPerCharacter;
    }

    @Override
    public void onGameEnding(GameModel gameModel, Player player) {
        super.onGameEnding(gameModel, player);
        player.getStats().addPoints(pointsPerCharacter * player.getStats().getCharacterTypeNum(characterType));
    }
}