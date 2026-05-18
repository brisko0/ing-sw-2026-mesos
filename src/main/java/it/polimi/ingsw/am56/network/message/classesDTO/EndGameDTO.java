package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;

public record EndGameDTO (
        int rank,
        String nickname,
        int score,
        int food,
        boolean isWinner,
        String date
) implements Serializable {}