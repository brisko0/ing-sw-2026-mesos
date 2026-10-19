package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;
import java.util.List;


/**
 * Transport snapshot of the shared central board. The card rows are sent as lists of card ids
 * (resolved back to full cards on the client via its local card catalog) rather than the cards
 * themselves, keeping the payload small and decoupling the wire format from the model classes.
 *
 * @param upperBuildingsIds card ids of the upper building row
 * @param lowerBuildingsIds card ids of the lower building row
 * @param upperCharEvIds    card ids of the upper character / event row
 * @param lowerCharEvIds    card ids of the lower character / event row
 * @param offerTilesLetters letters identifying the offer tiles, in board order
 * @param orderTileCount    number of order tiles still available on the board
 * @param eventCardIds      card ids of the cards in either row that are Event cards (a subset of
 *                          {@code upperCharEvIds} / {@code lowerCharEvIds})
 */
public record BoardDTO(
        List<Integer> upperBuildingsIds,
        List<Integer> lowerBuildingsIds,
        List<Integer> upperCharEvIds,
        List<Integer> lowerCharEvIds,
        List<String> offerTilesLetters,
        int orderTileCount,
        List<Integer> eventCardIds
) implements Serializable {}