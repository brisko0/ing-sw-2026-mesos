package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.card.BuildingCard;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

public class CardPicking extends GameState {

    public CardPicking(GameModel model) {
        super(model);
        checkTurnFlow();
    }

    private void checkTurnFlow() {
        Player current = model.getCurrentPlayer();

        // Se non c'è più nessuno che deve pescare, passiamo alla fine round
        if (model.getPlayerToOfferTile().isEmpty()) {
            model.setState(new RoundEnding(model));
            model.setCurrentPlayer(null);
            return;
        }

        // Se il giocatore attuale non può più pescare (slot pieni o cibo finito)
        if (!model.checkCanPick(current)) {
            model.resetTotemToOrder(current);
            model.updateCurrentPlayer();
            checkTurnFlow();
        }
    }

    @Override
    public void pickUpCard(Player p, Card c) throws InvalidActionException {
        if (!p.equals(model.getCurrentPlayer())) {
            throw new InvalidActionException("Non è il tuo turno! Deve muovere il player " + model.getCurrentPlayer().getNickname());
        }

        boolean isUpper = ( model.getBoard().getUpperRowBuildings().contains(c) ||
                model.getBoard().getUpperRowCharEv().contains(c));

        if (!isUpper & !model.getBoard().getLowerRowCharEv().contains(c) && !model.getBoard().getLowerRowBuildings().contains(c)) {
            throw new InvalidActionException("La carta selezionata non è disponibile sulla board.");
        }

        OfferTile ot = model.getPlayerToOfferTile().get(p);

        if (isUpper) {
            if (p.getPickedUpper() >= ot.getUpperCards()) {
                throw new InvalidActionException("Hai già pescato il numero massimo di carte dalla riga superiore!");
            }
        } else {
            if (p.getPickedLower() >= ot.getLowerCards()) {
                throw new InvalidActionException("Hai già pescato il numero massimo di carte dalla riga inferiore!");
            }
        }

        int cost = 0;
        if (c instanceof BuildingCard building) {
            cost = building.getCost() - p.getStats().getBuildingSale();
            if (p.getStats().getFood() < cost) {
                throw new InvalidActionException("Cibo insufficiente! Ti servono " + cost + " unità di cibo.");
            }
        }

        model.executeCardPick(p, c, isUpper, cost);

        checkTurnFlow();

    }
}