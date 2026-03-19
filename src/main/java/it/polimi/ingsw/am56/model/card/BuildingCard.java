package it.polimi.ingsw.am56.model.card;

public class BuildingCard extends Card{
    private final int cost;
    private final int points;

    public BuildingCard(int era, int numPlayers, int cost, int points) {
        super(era, numPlayers);
        this.cost = cost;
        this.points = points;
    }

    @Override
    public void resolveEffect() {}
}
