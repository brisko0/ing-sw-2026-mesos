package it.polimi.ingsw.am56.controller;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.board.OfferTile;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.exceptions.InvalidActionException;
import it.polimi.ingsw.am56.network.message.request.*;

public class GameController {
    private final GameModel model;

    public GameController(GameModel model) {
        this.model = model;
    }

    public void addPlayer(String nickname, String totemColor) throws InvalidActionException {
        synchronized (model) {

            if (model.getPlayers().stream().anyMatch(p -> p.getNickname().equals(nickname))) {
                throw new InvalidActionException("Nickname già in uso!");
            }
            if (model.getPlayers().size() == model.getGameSize()) {
                throw new InvalidActionException("Partita già al completo!");
            }
            model.addPlayer(new Player(nickname, totemColor));
            System.out.println("[CONTROLLER] Aggiunto giocatore: " + nickname);
        }
    }

    public void startGame() throws InvalidActionException {
        synchronized (model) {
            model.initializeGame(2);
            System.out.println("[CONTROLLER] Partita inizializzata con " + model.getPlayers().size() + " giocatori.");
        }
    }

    public void moveTotem(TotemMoveRequest totemRequest) throws InvalidActionException {
        synchronized (model) {

            Player player = model.getPlayers().stream()
                    .filter(p -> p.getNickname().equals(totemRequest.nickname()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidActionException("Giocatore non trovato"));

            if (!player.equals(model.getCurrentPlayer())) {
                throw new InvalidActionException("Non è il tuo turno!");
            }

            OfferTile tile = model.getBoard().getOfferTiles().stream()
                    .filter(t -> t.getLetter().equals(totemRequest.offerTileLetter()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidActionException("Tessera offerta non valida"));

            model.handleTotemPlacement(player, tile);

            System.out.println("[CONTROLLER] Totem mosso per " + totemRequest.nickname() + " sulla tile " + totemRequest.offerTileLetter());
        }
    }

    public void cardPick(CardPickRequest cardPickRequest) throws InvalidActionException {
        synchronized (model) {

            Player player = model.getPlayers().stream()
                    .filter(p -> p.getNickname().equals(cardPickRequest.nickname()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidActionException("Giocatore non trovato"));

            if (!player.equals(model.getCurrentPlayer())) {
                throw new InvalidActionException("Non è il tuo turno!");
            }

            Card card = model.getBoard().getCardById(cardPickRequest.cardId());

            model.pickUpCard(player, card);
            System.out.println("[CONTROLLER] carta pescata per " + cardPickRequest.nickname() + ", carta con id " + cardPickRequest.cardId());

        }
    }
}