package it.polimi.ingsw.am56.model.factory.board;

import it.polimi.ingsw.am56.model.board.Board;

/**
 * Setup-time helper that builds a fresh {@link Board} populated with the offer and order
 * tiles appropriate for the current game size. The card rows are intentionally left empty:
 * they are filled later by the board's own {@code fill*} methods once the decks are ready.
 * Delegates the tile filtering to {@link OfferTilesFactory} and {@link OrderTilesFactory}.
 */
public class BoardFactory {

    /**
     * Creates a {@link Board} whose offer-tiles and order-tiles lists are filled with the
     * subset valid for the given player count.
     *
     * @param gameSize number of players in the game
     * @return a board with offer / order tiles populated and empty card rows
     */
    public static Board createBoard(int gameSize){
        Board board = new Board();

        OfferTilesFactory offerTilesFactory = new OfferTilesFactory();
        OrderTilesFactory orderTilesFactory = new OrderTilesFactory();

        board.getOfferTiles().addAll(offerTilesFactory.createOfferTiles(gameSize));
        board.getOrderTiles().addAll(orderTilesFactory.createOrderTiles(gameSize));

        return board;
    }
}
