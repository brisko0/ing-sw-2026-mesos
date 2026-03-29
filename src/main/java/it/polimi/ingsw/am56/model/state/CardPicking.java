package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.card.BuildingCard;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.card.CharacterCard;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

public class CardPicking extends GameState {

    public CardPicking(GameModel model) {
        super(model);
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
    public void pickUpCharacterCard(Player p, CharacterCard c) throws InvalidActionException {

        if (!p.equals(model.getCurrentPlayer())) {
            throw new InvalidActionException("Non è il tuo turno! Deve muovere il player " + model.getCurrentPlayer().getNickname());
        }

        boolean isUpper = model.getBoard().getUpperRowCharEv().contains(c);

        if (!isUpper && !model.getBoard().getLowerRowCharEv().contains(c)) {
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

        model.executeCharacterCardPick(p, c, isUpper);

        checkTurnFlow();
    }

    public void pickUpBuildingCard(Player p, BuildingCard c) throws InvalidActionException {

        if (!p.equals(model.getCurrentPlayer())) {
            throw new InvalidActionException("Non è il tuo turno! Deve muovere il player " + model.getCurrentPlayer().getNickname());
        }

        boolean isUpper = model.getBoard().getUpperRowBuildings().contains(c);

        if (!isUpper && !model.getBoard().getLowerRowBuildings().contains(c)) {
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


        if (p.getStats().getFood() < (c.getCost() - p.getStats().getBuildingSale())) {
            throw new InvalidActionException("Cibo insufficiente! non è possibile prendere la carta edificio.");
        }

        model.executeBuildingCardPick(p, c, isUpper);

        checkTurnFlow();

    }
}