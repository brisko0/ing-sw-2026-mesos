package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;
import java.util.List;


public record BoardDTO(
        List<Integer> upperBuildingsIds,
        List<Integer> lowerBuildingsIds,
        List<Integer> upperCharEvIds,
        List<Integer> lowerCharEvIds,
        List<String> offerTilesLetters,
        int orderTileCount,
        List<Integer> eventCardIds
) implements Serializable {}