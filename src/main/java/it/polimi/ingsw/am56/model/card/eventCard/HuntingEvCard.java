package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;

import java.io.Serial;

public class HuntingEvCard extends EventCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int winPointsPerHunter;

    public HuntingEvCard(int cardId, int era, int minPlayers, int winPointsPerHunter) {
        super(cardId, era, minPlayers);
        this.winPointsPerHunter = winPointsPerHunter;
    }

    @Override
    public void resolveEffect(GameModel gameModel) {
        for(Player player : gameModel.getPlayers()) {
            int huntersNum = player.getStats().getCharacterTypeNum(CharacterType.HUNTER);
            player.getStats().addFood(huntersNum);
            player.getStats().addPoints(winPointsPerHunter * huntersNum);
        }
    }

    @Override
    public EvType getEventType() {
        return EvType.HUNTING;
    }

}
