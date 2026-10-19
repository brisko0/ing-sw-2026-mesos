package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

import java.io.Serial;

/**
 * Turn-order phase of each round: every player, in the order dictated by their order tile,
 * claims one free {@link OfferTile} by moving their totem onto it. When the last player has
 * placed, the model is re-iterated in offer-tile order and the state advances to
 * {@link CardPicking}.
 */
public class TotemPlacement extends GameState{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates the totem-placement state.
     *
     * @param model the game model this state operates on
     */
    public TotemPlacement(GameModel model) {
        super(model);
    }

    /**
     * Validates and performs a totem placement. Rejects the call when it is not the given
     * player's turn or when the chosen tile is already claimed. After applying the move,
     * advances to the next player still waiting in order; once everyone has placed, the
     * model is rolled over to offer-tile order and the state machine transitions to
     * {@link CardPicking}.
     *
     * @param p the player placing the totem
     * @param t the offer tile being claimed
     * @throws InvalidActionException if it is not {@code p}'s turn or if {@code t} is already claimed
     */
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

    /**
     * @return {@link State#TOTEM_PLACEMENT}
     */
    @Override
    public State getState() {
        return State.TOTEM_PLACEMENT;
    }
}
