package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

public class TotemPlacement extends GameState{

    public TotemPlacement(GameModel model) {
        super(model);
    }

    @Override
    public void handleTotemPlacement(Player p, OfferTile t) throws InvalidActionException {
        // il giocatore che ha cliccato è quello di turno, cioè currentPlayer?
        if (!p.equals(model.getCurrentPlayer())) {
            throw new InvalidActionException("Non è il tuo turno! Deve muovere il player " + model.getCurrentPlayer().getNickname());
        }

        // la tessera è libera?
        if (!t.isFree()) {
            throw new InvalidActionException("Questa tessera è già stata scelta. Scegline un'altra per il tuo totem!");
        }

        // controllo integrità: il giocatore ha già un totem sulla board?
        if (model.getPlayerToOfferTile().containsKey(p)) {
            throw new InvalidActionException("Hai già piazzato il tuo totem per questo round.");
        }

        // controlli finiti

        // chiama metodo stupido
        model.executeTotemPlacement(p, t);

        if(!model.getPlayerToOrderTile().isEmpty()) {
            model.setCurrentPlayerOrder();
            return;
        }

        model.setCurrentPlayerOffer();
        model.setState(new CardPicking(model));
    }
}
