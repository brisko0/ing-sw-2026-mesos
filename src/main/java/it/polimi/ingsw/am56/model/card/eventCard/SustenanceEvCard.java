package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;

import java.io.Serial;

public class SustenanceEvCard extends EventCard {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int losePointsPerChar;

    public SustenanceEvCard(int cardId, int era, int minPlayers, int losePointsPerCharSustenance) {
        super(cardId, era, minPlayers);
        this.losePointsPerChar = losePointsPerCharSustenance;
    }

    @Override
    public void resolveEffect(GameModel gameModel) {
        for(Player player : gameModel.getPlayers()) {
            int playerFood = player.getStats().getFood();
            int haveToPay = Math.max(player.getStats().getCharacterCards().size() - player.getStats().getFeedingSale(), 0);
            if(playerFood >= haveToPay) {
                player.getStats().addFood(-haveToPay);
            }
            else {
                player.getStats().addFood(-playerFood);
                player.getStats().addPoints(-losePointsPerChar * (haveToPay - playerFood));
            }
        }
    }

    @Override
    public EvType getEventType() {
        return EvType.SUSTENANCE;
    }

}
