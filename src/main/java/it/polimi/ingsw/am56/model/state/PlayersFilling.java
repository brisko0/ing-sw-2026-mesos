package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

import java.io.Serial;
import java.util.List;

/**
 * Initial state — the lobby. Accepts {@link #addPlayer(Player)} calls until the configured
 * {@code gameSize} is reached, validating uniqueness of nickname and totem colour as players
 * join. When the last seat is filled it runs the per-seat setup (initial food per turn order
 * + binding each player to their starting order tile) and transitions to {@link TotemPlacement}.
 * <p>
 * All other state machine actions inherit the rejecting defaults from {@link GameState}.
 */
public class PlayersFilling extends GameState{
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates the lobby state.
     *
     * @param model the game model this state operates on
     */
    public PlayersFilling(GameModel model) {
        super(model);
    }

    /**
     * Validates and registers a new player. The nickname (case-insensitive) and totem colour
     * must not already be taken, and the lobby must not yet be full. When this join brings the
     * lobby to {@code gameSize}, runs the per-seat setup and transitions the model to
     * {@link TotemPlacement}.
     *
     * @param p the player attempting to join
     * @throws InvalidActionException if the nickname or totem colour is taken, or the lobby is full
     */
    @Override
    public void addPlayer(Player p) throws InvalidActionException {
        for (Player existing : model.getPlayers()) {
            if (existing.getNickname().equalsIgnoreCase(p.getNickname())) {
                throw new InvalidActionException("Nickname già in uso!");
            }
        }

        for (Player existing : model.getPlayers()) {
            if (existing.getTotemColor().equalsIgnoreCase(p.getTotemColor())) {
                throw new InvalidActionException("Colore del totem non disponibile!");
            }
        }

        if (model.getPlayers().size() == model.getGameSize()) {
            throw new InvalidActionException("Impossibile unirsi, partita già al completo!");
        }

        model.executeAddPlayer(p);

        if(model.getPlayers().size() == model.getGameSize()){
            setUpPlayers(model);
            model.setState(new TotemPlacement(model));
            model.setCurrentPlayerOrder();
        }
    }

    private void setUpPlayers(GameModel model){
        List<Player> players = model.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            switch(i) {
                case 0 -> model.getPlayers().get(i).getStats().addFood(2);
                case 1, 2 -> model.getPlayers().get(i).getStats().addFood(3);
                case 3, 4 -> model.getPlayers().get(i).getStats().addFood(4);
            }
            model.getPlayerToOrderTile().put(players.get(i), model.getBoard().getOrderTiles().get(i));
            model.getBoard().getOrderTiles().get(i).setIsFree(false);
        }
    }

    /**
     * @return {@link State#PLAYERS_FILLING}
     */
    @Override
    public State getState() {
        return State.PLAYERS_FILLING;
    }
}
