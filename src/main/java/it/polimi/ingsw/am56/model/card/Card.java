package it.polimi.ingsw.am56.model.card;

public abstract class Card {
    private final int era;
    private final int numPlayers;

    public Card(int era, int numPlayers){
        this.era = era;
        this.numPlayers = numPlayers;
    }

    public abstract void resolveEffect();

    public int getEra() {
        return era;
    }

    public int getNumPlayers() {
        return numPlayers;
    }
}
