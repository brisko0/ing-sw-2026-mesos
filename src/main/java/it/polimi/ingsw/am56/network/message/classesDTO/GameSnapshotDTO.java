package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;
import java.util.List;

public record GameSnapshotDTO(
        BoardDTO board,
        List<PlayerStatsDTO> playersStats,
        List<TotemPositionDTO> totems,
        CurrentPlayerDTO currentPlayer,
        GameStateDTO gameStateDTO,
        int currentEra,
        int currentRound
) implements Serializable {}