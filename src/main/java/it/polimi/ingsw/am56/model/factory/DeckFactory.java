package it.polimi.ingsw.am56.model.factory;

import it.polimi.ingsw.am56.model.card.Deck;


public interface DeckFactory {
    Deck createDeck(int era, int gameSize);
}
