package it.polimi.ingsw.am56.model.card;

import it.polimi.ingsw.am56.model.card.characterCard.CharacterCard;
import it.polimi.ingsw.am56.model.card.eventCard.EventCard;

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

    public static Deck createCharEvDeck(int era, int gameSize){
        List<CharacterCard> characters = CharacterCard.createCharCards(era, gameSize);
        List<EventCard> events = EventCard.createEvCards(era, gameSize);

        List<Card> fullList = new ArrayList<>();
        fullList.addAll(characters);
        fullList.addAll(events);

        Deck deck = new Deck(era,era,fullList);
        return deck;
    }

    public static Deck createBuildingsDeck(int era, int gameSize){
        List<Card> buildings = BuildingCard.createBuildingCards(era, gameSize);

        Deck deck = new Deck(era + 3,era, buildings);

        return deck;
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card drawCard() {
        if(cards == null || cards.isEmpty()) {
            return null;
        }
        return cards.remove(0);
    }

    public int getRemainingCards() {
        return cards.size();
    }

    public boolean isEmpty() {
        return getRemainingCards() == 0;
    }
}
