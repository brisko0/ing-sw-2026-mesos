package it.polimi.ingsw.am56.model.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final int deckId;
    private final int era;
    private List<Card> cards;

    public Deck(int deckId, int era, List<Card> cards) {
        this.deckId = deckId;
        this.era = era;
        this.cards = new ArrayList<>(cards);
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card drawCard() {
        return cards.remove(0);
    }

    public List<Card> getRemainingCards() {
        return cards;
    }

    public boolean isEmpty() {
        return getRemainingCards().isEmpty();
    }
}
