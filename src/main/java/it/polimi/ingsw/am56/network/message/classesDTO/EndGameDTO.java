package it.polimi.ingsw.am56.network.message.classesDTO;

public record EndGameDTO(
        int rank,
        String nickname,
        int score,
        int food,
        boolean isWinner,
        String date
) {}