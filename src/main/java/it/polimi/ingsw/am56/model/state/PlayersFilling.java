package it.polimi.ingsw.am56.model.state;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

import java.util.List;

public class PlayersFilling extends GameState{

    public PlayersFilling(GameModel model) {
        super(model);
    }

    @Override
    public void addPlayer(Player p) throws InvalidActionException {

        for (Player existing : model.getPlayers()) {
            if (existing.getNickname().equalsIgnoreCase(p.getNickname())) {
                throw new InvalidActionException("Nickname già in uso!");
            }
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
            players.get(i).getStats().setInitialFood(i + 1);
            model.getPlayerToOrderTile().put(players.get(i), model.getBoard().getOrderTiles().get(i));
            model.getBoard().getOrderTiles().get(i).setIsFree(false);
        }
    }
}
