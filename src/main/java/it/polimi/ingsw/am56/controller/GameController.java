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

    public void createGame(String nickname, String totemColor, int gameSize) throws InvalidActionException {
        synchronized (model) {
            // inizializzazione del model con la size scelta dal creatore del game
            model.initializeGame(gameSize);

            // aggiungo il creatore
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

    public void moveTotem(String nickname, String offerTileLetter) throws InvalidActionException {
        synchronized (model) {

            // risalgo al player dal nickname
            Player player = model.getPlayers().stream()
                    .filter(p -> p.getNickname().equals(nickname))
                    .findFirst()
                    .orElseThrow(() -> new InvalidActionException("Giocatore non trovato"));

            // recupero l'OfferTile dal suo Id
            OfferTile tile = model.getBoard().getOfferTiles().stream()
                    .filter(t -> t.getLetter().equals(offerTileLetter))
                    .findFirst()
                    .orElseThrow(() -> new InvalidActionException("Tessera offerta non valida"));

            // delego al modello. Tutti i controlli sono eseguiti nello state pattern del modello
            // perciò non sono presenti qui nel controller
            model.handleTotemPlacement(player, tile);

            System.out.println("[CONTROLLER] Totem mosso per " + nickname + " sulla tile " + offerTileLetter);
        }
    }

    public void cardPick(String nickname, int cardId) throws InvalidActionException {
        synchronized (model) {

            // risalgo al player dal nickname
            Player player = model.getPlayers().stream()
                    .filter(p -> p.getNickname().equals(nickname))
                    .findFirst()
                    .orElseThrow(() -> new InvalidActionException("Giocatore non trovato"));

            // recupero la card dalla board attraverso l'id
            Card card = model.getBoard().getCardById(cardId);

            // delego al modello. Tutti i controlli sono eseguiti nello state pattern del modello
            // perciò non sono presenti qui nel controller
            model.pickUpCard(player, card);
            System.out.println("[CONTROLLER] carta pescata per " + nickname + ", carta con id " + cardId);

        }
    }
}