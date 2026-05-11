package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;
import java.util.List;

public record EndRoundSnapshotDTO(
        BoardDTO board,
        List<PlayerStatsDTO> playersStats,
        CurrentPlayerDTO currentPlayer,
        int currentEra,
        int currentRound
) implements Serializable {}