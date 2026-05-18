package it.polimi.ingsw.am56.controller;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;

public class GameController {
    private final GameModel model;

    public GameController(GameModel model) {
        this.model = model;
    }

    public void checkLobbyState() {
        synchronized (model) {
            model.syncLobbyState();
        }
    }

    public void sendGameState() {
        synchronized (model) {
            model.syncGameState();
        }
    }

    public void createGame(String nickname, String totemColor, int gameSize) throws InvalidActionException {
        synchronized (model) {
            model.initializeGame(gameSize);
            model.addPlayer(new Player(nickname, totemColor));
            System.out.println("[CONTROLLER] Partita creata da " + nickname + " per " + gameSize + " giocatori.");
        }
    }

    public void addPlayer(String nickname, String totemColor) throws InvalidActionException {
        synchronized (model) {
            model.addPlayer(new Player(nickname, totemColor));
            System.out.println("[CONTROLLER] Aggiunto giocatore: " + nickname);
        }
    }

    public void reconnectPlayer(String nickname) throws InvalidActionException {
        synchronized (model) {
            if (!model.hasPlayer(nickname)) {
                throw new InvalidActionException(
                        "Nickname '" + nickname + "' non trovato nella partita in corso. " +
                                "Devi usare lo stesso nickname con cui hai iniziato la partita.");
            }
            System.out.println("[CONTROLLER] Riconnessione di: " + nickname);
            model.syncGameState();
        }
    }

    public void moveTotem(String nickname, String offerTileLetter) throws InvalidActionException {
        synchronized (model) {
            Player player = model.getPlayers().stream()
                    .filter(p -> p.getNickname().equals(nickname))
                    .findFirst()
                    .orElseThrow(() -> new InvalidActionException("Giocatore non trovato"));

            OfferTile tile = model.getBoard().getOfferTiles().stream()
                    .filter(t -> t.getLetter().equals(offerTileLetter))
                    .findFirst()
                    .orElseThrow(() -> new InvalidActionException("Tessera offerta non valida"));

            model.handleTotemPlacement(player, tile);
            System.out.println("[CONTROLLER] Totem mosso per " + nickname + " sulla tile " + offerTileLetter);
        }
    }

    public void cardPick(String nickname, int cardId) throws InvalidActionException {
        synchronized (model) {
            Player player = model.getPlayers().stream()
                    .filter(p -> p.getNickname().equals(nickname))
                    .findFirst()
                    .orElseThrow(() -> new InvalidActionException("Giocatore non trovato"));

            Card card = model.getBoard().getCardById(cardId);
            model.pickUpCard(player, card);
            System.out.println("[CONTROLLER] carta pescata per " + nickname + ", carta con id " + cardId);
        }
    }
}