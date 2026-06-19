package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;
import java.util.List;

/**
 * Transport snapshot of a single player's in-game state. The character hand is sent as one id
 * list per archetype (rather than counts) so the client can render the actual cards, and the
 * affordability / pick flags are pre-computed server-side so the views don't need to re-derive
 * the rules.
 *
 * @param collectorCards        card ids of the player's Collector character cards
 * @param hunterCards           card ids of the player's Hunter character cards
 * @param builderCards          card ids of the player's Builder character cards
 * @param shamanCards           card ids of the player's Shaman character cards
 * @param artistCards           card ids of the player's Artist character cards
 * @param inventorCards         card ids of the player's Inventor character cards
 * @param nickname              the player's nickname
 * @param color                 the player's totem colour
 * @param food                  the player's current food
 * @param points                the player's current victory points
 * @param feedingSale           cumulative feeding discount the player has accrued
 * @param buildingSale          cumulative building-purchase discount the player has accrued
 * @param shamanStars           the player's shaman-stars total
 * @param inventionCount        number of distinct inventions the player has discovered
 * @param canPickUpper          whether the player may still pick a card from the upper row this turn
 * @param canPickLower          whether the player may still pick a card from the lower row this turn
 * @param affordableBuildingIds card ids of the board buildings the player can currently afford
 *                              (food at least the cost minus {@code buildingSale})
 * @param ownedBuildingCards    card ids of the building cards the player already owns
 */
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
        boolean canPickLower,

        List<Integer> affordableBuildingIds,

        List<Integer> ownedBuildingCards
) implements Serializable {}