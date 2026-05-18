package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

import java.io.Serial;

public class CardPicking extends GameState {
    @Serial
    private static final long serialVersionUID = 1L;

    public CardPicking(GameModel model) {
        super(model);
    }

    @Override
    public void onEntry() {
        checkTurnFlow();
    }

    private void checkTurnFlow() {

        // Se il giocatore attuale non può più pescare (slot pieni o cibo finito)
        if (!model.checkCanPick()) {
            model.resetTotemToOrder();

            if(model.getPlayerToOfferTile().isEmpty()) {
                model.setState(new RoundEnding(model));
                return;
            }

            model.setCurrentPlayerOffer();
            checkTurnFlow();
        }
    }

    @Override
    public void pickUpCard(Player p, Card c) throws InvalidActionException {

        if (!p.equals(model.getCurrentPlayer())) {
            throw new InvalidActionException("Non è il tuo turno! Deve scegliere una carta il player " + model.getCurrentPlayer().getNickname());
        }

        boolean isUpper = model.getBoard().getUpperRowCharEv().contains(c) || model.getBoard().getUpperRowBuildings().contains(c);

        if (!isUpper && !model.getBoard().getLowerRowCharEv().contains(c) && !model.getBoard().getLowerRowBuildings().contains(c)) {
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

        if(model.getCurrentPlayer().getStats().getFood() < (c.getCost() - model.getCurrentPlayer().getStats().getBuildingSale())) {
            throw new InvalidActionException("non hai il cibo necessario per permetterti la carta building");
        }

        model.executeCardPick(p, c, isUpper);

        checkTurnFlow();
    }

    @Override
    public State getState() {
        return State.CARD_PICKING;
    }
}