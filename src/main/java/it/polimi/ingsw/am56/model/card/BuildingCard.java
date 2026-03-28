package it.polimi.ingsw.am56.model.card;

import it.polimi.ingsw.am56.model.GameModel;

public class BuildingCard extends Card{
    private final int cost;
    private final int points;

    public BuildingCard(int idCard, int era, int numPlayers, int cost, int points) {
        super(idCard,era, numPlayers);
        this.cost = cost;
        this.points = points;
    }

    @Override
    public void resolveEffect(GameModel gameModel) {}
}
