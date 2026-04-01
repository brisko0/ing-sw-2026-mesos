package it.polimi.ingsw.am56.model.factory;

import it.polimi.ingsw.am56.model.card.Card;

import java.util.List;

public interface CardFactory<T extends Card> {
    List<T> createCards(int era, int gameSize);
}