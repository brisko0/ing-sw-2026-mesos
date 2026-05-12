package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;
import java.util.List;

public record PlayerStatsDTO(
        String nickname,
        String color,
        int food,
        int points,
        List<Integer> collectorCards,
        List<Integer> hunterCards,
        List<Integer> builderCards,
        List<Integer> shamanCards,
        List<Integer> artistCards,
        List<Integer> inventorCards,
        int feedingSale,
        int buildingSale,
        int shamanStars,
        int inventionCount,

        boolean canPickUpper,
        boolean canPickLower
) implements Serializable {}