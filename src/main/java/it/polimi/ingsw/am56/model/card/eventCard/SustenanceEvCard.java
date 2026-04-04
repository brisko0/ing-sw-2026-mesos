package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;

public class SustenanceEvCard extends EventCard {
    private final int losePointsPerChar;

    public SustenanceEvCard(int cardId, int era, int minPlayers, int losePointsPerCharSustenance) {
        super(cardId, era, minPlayers);
        this.losePointsPerChar = losePointsPerCharSustenance;
    }

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

    public EvType getEventType() {
        return EvType.SUSTENANCE;
    }
}