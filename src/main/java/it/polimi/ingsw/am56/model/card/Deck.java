package it.polimi.ingsw.am56.model.card;

import java.util.ArrayList;
import java.util.List;

public class Deck {
    private final int deckId;
    private final int era;
    private List<Card> cards;

    public Deck(int deckId, int era, List<Card> cards) {
        this.deckId = deckId;
        this.era = era;
        this.cards = new ArrayList<>(cards);
    }

    public void shuffle() { }

    public Card drawCard() {
        return cards.get(0);
    }

    public int getRemainingCards() {
        return cards.size();
    }

    public boolean isEmpty() {
        return getRemainingCards() == 0;
    }
}
