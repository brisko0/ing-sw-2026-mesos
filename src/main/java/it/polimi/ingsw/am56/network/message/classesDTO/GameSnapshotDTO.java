package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;
import java.util.List;

/**
 * Full snapshot of the game, carrying everything a client needs to (re)build its view from
 * scratch. Sent when the game starts and on resync (e.g. after a reconnection), so it includes
 * the totem positions and the current game phase that the lighter per-event updates omit.
 *
 * @param board         the current board state
 * @param playersStats  the stats of every player
 * @param totems        the current positions of all placed totems
 * @param currentPlayer the player whose turn it currently is
 * @param gameStateDTO  the current game phase
 * @param currentEra    the current era (1, 2 or 3)
 * @param currentRound  the current round number
 */
public record GameSnapshotDTO(
        BoardDTO board,
        List<PlayerStatsDTO> playersStats,
        List<TotemPositionDTO> totems,
        CurrentPlayerDTO currentPlayer,
        GameStateDTO gameStateDTO,
        int currentEra,
        int currentRound
) implements Serializable {}