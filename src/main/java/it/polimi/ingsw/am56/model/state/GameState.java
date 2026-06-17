package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

import java.io.Serial;
import java.io.Serializable;

/**
 * Base class for the State pattern that drives the game lifecycle. The {@link GameModel}
 * delegates every player action ({@link #addPlayer}, {@link #handleTotemPlacement},
 * {@link #pickUpCard}) to its current {@link GameState}; subclasses override only the action
 * that is legal in their phase and let the others fall through to the default implementations
 * here, which reject the call with {@link InvalidActionException}.
 * <p>
 * Concrete states are {@link PlayersFilling}, {@link TotemPlacement}, {@link CardPicking},
 * {@link RoundEnding} and {@link GameEnding}; the discriminator returned by {@link #getState()}
 * matches one of the {@link State} enum values.
 */
public abstract class GameState implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    protected final GameModel model;

    /**
     * Creates the state, retaining a back-reference to the model it drives.
     *
     * @param model the game model this state operates on
     */
    public GameState(GameModel model) {
        this.model = model;
    }

    /**
     * Returns the discriminator for this state. Used by the view layer and by serialisation
     * to identify the current phase without instanceof checks.
     *
     * @return the {@link State} enum value matching this concrete subclass
     */
    public abstract State getState();

    /**
     * Lobby-only action: registers a new player. The default implementation rejects the call;
     * {@link PlayersFilling} overrides it with the actual validation and join logic.
     *
     * @param p the player attempting to join
     * @throws InvalidActionException if joining is not legal in the current phase
     */
    public void addPlayer(Player p) throws InvalidActionException {
        throw new InvalidActionException("Cannot add players in this phase.");
    }

    /**
     * Totem-placement action: the given player claims the given offer tile. The default
     * implementation rejects the call; {@link TotemPlacement} overrides it.
     *
     * @param p the player placing the totem
     * @param t the offer tile being claimed
     * @throws InvalidActionException if totem placement is not legal in the current phase
     */
    public void handleTotemPlacement(Player p, OfferTile t) throws InvalidActionException {
        throw new InvalidActionException("Cannot place totems right now.");
    }

    /**
     * Card-picking action: the given player picks the given card off the board. The default
     * implementation rejects the call; {@link CardPicking} overrides it.
     *
     * @param p the player picking the card
     * @param c the card being picked
     * @throws InvalidActionException if card picking is not legal in the current phase
     */
    public void pickUpCard(Player p, Card c) throws InvalidActionException {
        throw new InvalidActionException("Cannot pick up cards right now.");
    }

    /**
     * Extra-pick action: the given player <em>declines</em> the optional extra upper-row pick
     * granted by their building, ending the round without taking a card. The default
     * implementation rejects the call; {@link ExtraUpperCardPicking} overrides it.
     *
     * @param p the player declining the extra pick
     * @throws InvalidActionException if there is no optional extra pick to skip in the current phase
     */
    public void skipExtraUpperCardPick(Player p) throws InvalidActionException {
        throw new InvalidActionException("There is no optional pick to skip right now.");
    }

    /**
     * Hook fired by {@link GameModel#setState} immediately after this state is installed.
     * The base implementation is a no-op; states such as {@link RoundEnding},
     * {@link GameEnding} and {@link CardPicking} override it to perform their automatic
     * actions or transitions on entry.
     */
    public void onEntry() {}
}
