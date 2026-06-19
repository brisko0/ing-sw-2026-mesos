package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;
import java.util.List;

/**
 * Snapshot sent to the clients when a round ends, after the board has advanced (rows shifted and
 * refilled). Unlike {@link GameSnapshotDTO} it omits the totem positions, since totems are reset
 * at end of round and re-broadcast separately.
 *
 * @param board         the board state after the end-of-round advance
 * @param playersStats  the stats of every player
 * @param currentPlayer the player who will act first in the new round
 * @param currentEra    the current era (1, 2 or 3) after the advance
 * @param currentRound  the current round number after the advance
 */
public record EndRoundSnapshotDTO(
        BoardDTO board,
        List<PlayerStatsDTO> playersStats,
        CurrentPlayerDTO currentPlayer,
        int currentEra,
        int currentRound
) implements Serializable {}