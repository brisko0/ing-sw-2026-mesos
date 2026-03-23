package it.polimi.ingsw.am56.model.board;

public class OfferTile {
    private final char letter;
    private final int upperCards;
    private final int lowerCards;
    private boolean isFree;

    public OfferTile(char letter, int upperCards, int lowerCards) {
        this.letter = letter;
        this.upperCards = upperCards;
        this.lowerCards = lowerCards;
        isFree = true;
    }

    public char getLetter() {
        return letter;
    }

    public int getUpperCards() {
        return upperCards;
    }

    public int getLowerCards() {
        return lowerCards;
    }

    public boolean isFree() {
        return isFree;
    }
}

