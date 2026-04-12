package it.polimi.ingsw.am56.model.board;

import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.Deck;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private List<OrderTile> orderTiles;
    private List<OfferTile> offerTiles;
    private List<Card> upperRowBuildings;
    private List<Card> upperRowCharEv;
    private List<Card> lowerRowBuildings;
    private List<Card> lowerRowCharEv;

    public Board(){
        orderTiles = new ArrayList<>();
        offerTiles = new ArrayList<>();
        upperRowBuildings = new ArrayList<>();
        upperRowCharEv = new ArrayList<>();
        lowerRowBuildings = new ArrayList<>();
        lowerRowCharEv = new ArrayList<>();
    }

    public List<OrderTile> getOrderTiles() {
        return orderTiles;
    }

    public List<OfferTile> getOfferTiles() {
        return offerTiles;
    }

    public List<Card> getUpperRowBuildings() {
        return upperRowBuildings;
    }

    public List<Card> getUpperRowCharEv() {
        return upperRowCharEv;
    }

    public List<Card> getLowerRowBuildings() {
        return lowerRowBuildings;
    }

    public List<Card> getLowerRowCharEv() {
        return lowerRowCharEv;
    }

    public void fillBoard(int gameSize, Deck charEvDeck, Deck buildingsDeck){
        while(lowerRowCharEv.size() < (gameSize + 1)){
            Card c = charEvDeck.drawCard();
            switch (c.getCardType()) {
                case CHARACTER -> lowerRowCharEv.add(c);
                case EVENT     -> upperRowCharEv.add(c);
                default        -> throw new IllegalStateException("Tipo sconosciuto: " + c.getCardType());
            }
        }

        fillUpperCharEv(gameSize, charEvDeck);
        fillUpperBuildings(gameSize, buildingsDeck);
    }

    public void fillUpperCharEv(int gameSize, Deck charEvDeck){
        while(upperRowCharEv.size() < (gameSize + 4)){
            upperRowCharEv.add(charEvDeck.drawCard());
        }
    }

    public void fillUpperBuildings(int gameSize, Deck buildingsDeck){
        while(buildingsDeck.getRemainingCards() > 0){
            upperRowBuildings.add(buildingsDeck.drawCard());
        }
    }

    public void removeCard(Card c){
        switch (c.getCardType()) {
            case CHARACTER -> {
                lowerRowCharEv.remove(c);
                upperRowCharEv.remove(c);
            }
            case BUILDING -> {
                upperRowBuildings.remove(c);
                lowerRowBuildings.remove(c);
            }
            default -> throw new IllegalStateException("Tipo sconosciuto: " + c.getCardType());
        }
    }

    public void endRound(int gameSize, Deck charEvDeck){
        lowerRowCharEv.clear();
        lowerRowCharEv.addAll(upperRowCharEv);
        upperRowCharEv.clear();
        fillUpperCharEv(gameSize, charEvDeck);
    }

    public void endEra(int gameSize, Deck charEvDeck, Deck buildingsDeck){
        lowerRowBuildings.clear();
        lowerRowBuildings.addAll(upperRowBuildings);
        upperRowBuildings.clear();
        fillUpperCharEv(gameSize, charEvDeck);
        fillUpperBuildings(gameSize, buildingsDeck);
    }


}
