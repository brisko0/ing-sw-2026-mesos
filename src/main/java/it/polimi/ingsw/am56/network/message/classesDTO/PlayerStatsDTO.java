package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;
import java.util.List;

public record PlayerStatsDTO(
        String nickname,
        String color,
        int food,
        int points,
        // Liste di ID invece di semplici interi per le carte tribù
        List<Integer> collectorCards,
        List<Integer> hunterCards,
        List<Integer> builderCards,
        List<Integer> shamanCards,
        List<Integer> artistCards,
        List<Integer> inventorCards,
        // Statistiche riga inferiore
        int feedingSale,
        int buildingSale,
        int shamanStars,
        int inventionCount,

        boolean canPickUpper,
        boolean canPickLower,

        List<Integer> affordableBuildingIds,

        List<Integer> ownedBuildingCards
) implements Serializable {}