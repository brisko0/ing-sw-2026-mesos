package it.polimi.ingsw.am56.model.board;

import it.polimi.ingsw.am56.model.card.BuildingDeck;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.Deck;
import it.polimi.ingsw.am56.model.card.buildingCard.BuildingCard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class Board {
    private List<OrderTile> orderTiles;
    private List<OfferTile> offerTiles;
    private List<BuildingCard> upperRowBuildings;
    private List<Card> upperRowCharEv;
    private List<BuildingCard> lowerRowBuildings;
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

    public List<BuildingCard> getUpperRowBuildings() {
        return upperRowBuildings;
    }

    public List<Card> getUpperRowCharEv() {
        return upperRowCharEv;
    }

    public List<BuildingCard> getLowerRowBuildings() {
        return lowerRowBuildings;
    }

    public List<Card> getLowerRowCharEv() {
        return lowerRowCharEv;
    }

    public void fillLowerChar(int gameSize, Deck charactersDeck){
        while(lowerRowCharEv.size() < (gameSize + 1)){
            lowerRowCharEv.add(charactersDeck.drawCard());
        }
    }

    public void fillUpperCharEv(int gameSize, Deck charEvDeck){
        while(upperRowCharEv.size() < (gameSize + 4) && !charEvDeck.getRemainingCards().isEmpty()){
            upperRowCharEv.add(charEvDeck.drawCard());
        }
    }

    public void fillUpperBuildings(int gameSize, BuildingDeck buildingsDeck, int era){
        switch(era) {
            case 1:
                if(gameSize == 2) {
                    for(int i = 0; i < 1; i++){
                        upperRowBuildings.add(buildingsDeck.drawCard());
                    }
                } else {
                    for(int i = 0; i < 2; i++){
                        upperRowBuildings.add(buildingsDeck.drawCard());
                    }
                }
                break;
            case 2:
                if(gameSize <= 3) {
                    for(int i = 0; i < 2; i++){
                        upperRowBuildings.add(buildingsDeck.drawCard());
                    }
                } else {
                    for(int i = 0; i < 3; i++){
                        upperRowBuildings.add(buildingsDeck.drawCard());
                    }
                }
                break;
            case 3:
                if(gameSize == 2) {
                    for(int i = 0; i < 3; i++){
                        upperRowBuildings.add(buildingsDeck.drawCard());
                    }
                } else if (gameSize == 5) {
                    for(int i = 0; i < 5; i++){
                        upperRowBuildings.add(buildingsDeck.drawCard());
                    }
                } else {
                    for(int i = 0; i < 4; i++){
                        upperRowBuildings.add(buildingsDeck.drawCard());
                    }
                }
                break;
        }
    }

    public void removeCard(Card c){
        List.of(lowerRowCharEv, upperRowCharEv, upperRowBuildings, lowerRowBuildings)
                .forEach(list -> list.remove(c));
    }

    public void endRound(int gameSize, Deck charEvDeck){
        lowerRowCharEv.clear();
        lowerRowCharEv.addAll(upperRowCharEv);
        upperRowCharEv.clear();
        fillUpperCharEv(gameSize, charEvDeck);
    }

    public void endEra(int gameSize, Deck charEvDeck, BuildingDeck buildingsDeck, int currentEra){
        lowerRowBuildings.clear();
        lowerRowBuildings.addAll(upperRowBuildings);
        upperRowBuildings.clear();
        fillUpperCharEv(gameSize, charEvDeck);
        fillUpperBuildings(gameSize, buildingsDeck, currentEra);
    }

    public Card getCardById(int cardId) {
        return Stream.of(upperRowCharEv, lowerRowCharEv, upperRowBuildings, lowerRowBuildings)
                .flatMap(Collection::stream)
                .filter(c -> c.getCardId() == cardId)
                .findFirst()
                .orElse(null);
    }
}
