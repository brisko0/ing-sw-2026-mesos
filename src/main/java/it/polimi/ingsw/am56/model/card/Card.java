package it.polimi.ingsw.am56.model.card;

public abstract class Card {
    private final int era;
    private final int numPlayers;
    private final int idCard;

    public Card(int idCard, int era, int numPlayers){
        this.idCard = idCard;
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

    public int getIdCard(){ return idCard; }
}
