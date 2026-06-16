package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

import java.io.Serial;

/**
 * Card-picking phase of each round: players, iterated in offer-tile order, resolve their
 * claimed {@link OfferTile} by drawing the allowed number of cards from the upper and lower
 * rows. Picks are constrained by the offer tile's row quotas and, for {@code BuildingCard}s,
 * by the player's available food (net of building sale discounts).
 * <p>
 * The state self-advances: {@link #onEntry()} and the tail of {@link #pickUpCard} both call
 * {@code checkTurnFlow}, which moves on to the next player whenever the current one can no
 * longer pick (row quotas exhausted or no affordable building), and transitions to
 * {@link ExtraUpperCardPicking} once every offer tile has been resolved.
 */
public class CardPicking extends GameState {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates the card-picking state.
     *
     * @param model the game model this state operates on
     */
    public CardPicking(GameModel model) {
        super(model);
    }

    /**
     * Drives the turn-flow check immediately on entry, so the state correctly skips players
     * that cannot pick (or transitions to {@link RoundEnding} if no one can).
     */
    @Override
    public void onEntry() {
        checkTurnFlow();
    }

    private void checkTurnFlow() {

        // Se il giocatore attuale non può più pescare (slot pieni o cibo finito)
        if (!model.checkCanPick()) {
            model.resetTotemToOrder();

            if(model.getPlayerToOfferTile().isEmpty()) {
                if (model.startExtraUpperCardPickingIfNeeded()) {
                    model.setState(new ExtraUpperCardPicking(model));
                } else {
                    model.setState(new RoundEnding(model));
                }
                return;
            }

            model.setCurrentPlayerOffer();
            checkTurnFlow();
        }
    }

    /**
     * Validates and performs a card pick. Rejects the call when it is not the given player's
     * turn, when the card is not currently on the board, when the player has used up the
     * offer tile's quota for that row, or when the player cannot afford the card. After
     * applying the pick, re-evaluates turn flow.
     *
     * @param p the player picking the card
     * @param c the card being picked
     * @throws InvalidActionException if the pick violates turn order, row quotas, board state,
     *                                or affordability
     */
    @Override
    public void pickUpCard(Player p, Card c) throws InvalidActionException {

        if (!p.equals(model.getCurrentPlayer())) {
            throw new InvalidActionException("Non è il tuo turno! Deve scegliere una carta il player " + model.getCurrentPlayer().getNickname());
        }

        boolean isUpper = model.getBoard().getUpperRowCharEv().contains(c) || model.getBoard().getUpperRowBuildings().contains(c);

        if (!isUpper && !model.getBoard().getLowerRowCharEv().contains(c) && !model.getBoard().getLowerRowBuildings().contains(c)) {
            throw new InvalidActionException("La carta selezionata non è disponibile sulla board.");
        }

        if (c.getCardType() == CardType.EVENT) {
            throw new InvalidActionException("Non è possibile pescare carte evento.");
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

        if(model.getCurrentPlayer().getStats().getFood() <
                Math.max(0, (c.getCost() - model.getCurrentPlayer().getStats().getBuildingSale()))) {
            throw new InvalidActionException("non hai il cibo necessario per permetterti la carta building");
        }

        model.executeCardPick(p, c, isUpper);

        checkTurnFlow();
    }

    /**
     * @return {@link State#CARD_PICKING}
     */
    @Override
    public State getState() {
        return State.CARD_PICKING;
    }
}
