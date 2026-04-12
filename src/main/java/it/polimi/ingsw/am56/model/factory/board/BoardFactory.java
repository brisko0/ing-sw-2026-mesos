package it.polimi.ingsw.am56.model.factory.board;

import it.polimi.ingsw.am56.model.board.Board;

public class BoardFactory {
    public static Board createBoard(int gameSize){
        Board board = new Board();

        OfferTilesFactory offerTilesFactory = new OfferTilesFactory();
        OrderTilesFactory orderTilesFactory = new OrderTilesFactory();

        board.getOfferTiles().addAll(offerTilesFactory.createOfferTiles(gameSize));
        board.getOrderTiles().addAll(orderTilesFactory.createOrderTiles(gameSize));

        return board;
    }
}
