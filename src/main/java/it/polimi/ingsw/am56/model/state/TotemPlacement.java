package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

import java.io.Serial;

public class TotemPlacement extends GameState{
    @Serial
    private static final long serialVersionUID = 1L;

    public TotemPlacement(GameModel model) {
        super(model);
    }

    @Override
    public void handleTotemPlacement(Player p, OfferTile t) throws InvalidActionException {

        if (!p.equals(model.getCurrentPlayer())) {
            throw new InvalidActionException("Non è il tuo turno! Deve muovere il player " + model.getCurrentPlayer().getNickname());
        }

        if (!t.isFree()) {
            throw new InvalidActionException("Questa tessera è già stata scelta. Scegline un'altra per il tuo totem");
        }

        model.executeTotemPlacement(p, t);

        if(!model.getPlayerToOrderTile().isEmpty()) {
            model.setCurrentPlayerOrder();
            return;
        }

        model.setCurrentPlayerOffer();
        model.setState(new CardPicking(model));
    }

    @Override
    public State getState() {
        return State.TOTEM_PLACEMENT;
    }
}
