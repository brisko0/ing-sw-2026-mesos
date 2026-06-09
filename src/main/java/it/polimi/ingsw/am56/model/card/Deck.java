package it.polimi.ingsw.am56.model.card;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shuffled, draw-from-top deck of cards. Construction defensively copies the supplied list
 * and shuffles it; {@link #drawCard()} removes the next card from the top. Used as the
 * generic deck for character / event rows; {@link BuildingDeck} specialises it for buildings.
 */
public class Deck implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int deckId;
    private final int era;
    private List<Card> cards;

    /**
     * Creates a deck holding a defensive copy of {@code cards} and shuffles it.
     *
     * @param deckId identifier of the deck
     * @param era    era the deck belongs to (1, 2 or 3)
     * @param cards  cards used to populate the deck (copied, not retained)
     */
    public Deck(int deckId, int era, List<Card> cards) {
        this.deckId = deckId;
        this.era = era;
        this.cards = new ArrayList<>(cards);
        shuffle();
    }

    /**
     * Re-shuffles the remaining cards in place.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Removes and returns the card on top of the deck.
     *
     * @return the drawn card
     * @throws IndexOutOfBoundsException if the deck is empty
     */
    public Card drawCard() {
        return cards.remove(0);
    }

    /**
     * Returns the live, mutable list of cards still in the deck.
     *
     * @return the remaining-cards list backing the deck
     */
    public List<Card> getRemainingCards() {
        return cards;
    }

    /**
     * Returns whether the deck has no cards left.
     *
     * @return {@code true} if the deck is empty
     */
    public boolean isEmpty() {
        return getRemainingCards().isEmpty();
    }
}
