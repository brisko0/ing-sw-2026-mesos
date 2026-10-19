package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

import java.io.Serial;

/**
 * Optional post-picking phase where players with the matching building may take extra cards
 * from the upper row before the round ends.
 */
public class ExtraUpperCardPicking extends GameState {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates the extra upper-card-picking state.
     *
     * @param model the game model this state operates on
     */
    public ExtraUpperCardPicking(GameModel model) {
        super(model);
    }

    /**
     * Validates and performs an extra pick from the upper row.
     *
     * @param p the player picking the card
     * @param c the card being picked
     * @throws InvalidActionException if the pick is not legal in this phase
     */
    @Override
    public void pickUpCard(Player p, Card c) throws InvalidActionException {
        if (!p.equals(model.getCurrentPlayer())) {
            throw new InvalidActionException("Non è il tuo turno! Deve scegliere una carta il player " + model.getCurrentPlayer().getNickname());
        }

        boolean isUpper = model.getBoard().getUpperRowCharEv().contains(c)
                || model.getBoard().getUpperRowBuildings().contains(c);

        if (!isUpper) {
            throw new InvalidActionException("La carta selezionata non è disponibile sulla riga superiore.");
        }

        if (c.getCardType() == CardType.EVENT) {
            throw new InvalidActionException("Non è possibile pescare carte evento.");
        }

        if (p.getStats().getFood() < Math.max(0, c.getCost() - p.getStats().getBuildingSale())) {
            throw new InvalidActionException("non hai il cibo necessario per permetterti la carta building");
        }

        model.executeExtraUpperCardPick(p, c);
        model.setState(new RoundEnding(model));
    }

    /**
     * Declines the optional extra upper-row pick: validates that it is the given player's turn
     * and then advances straight to {@link RoundEnding} without drawing any card. The extra
     * pick is optional by the rules (the building reads "you <em>may</em> take 1 card"), so a
     * player who does not want it must be able to skip.
     *
     * @param p the player declining the extra pick
     * @throws InvalidActionException if it is not {@code p}'s turn
     */
    @Override
    public void skipExtraUpperCardPick(Player p) throws InvalidActionException {
        if (!p.equals(model.getCurrentPlayer())) {
            throw new InvalidActionException("Non è il tuo turno! Deve agire il player " + model.getCurrentPlayer().getNickname());
        }
        model.setState(new RoundEnding(model));
    }

    /**
     * @return {@link State#EXTRA_UPPER_CARD_PICKING}
     */
    @Override
    public State getState() {
        return State.EXTRA_UPPER_CARD_PICKING;
    }
}
