package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.*;
import it.polimi.ingsw.am56.model.card.eventCard.ArtistsEvCard;
import it.polimi.ingsw.am56.model.card.eventCard.HuntingEvCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HuntingEventFoodPointsBonusTest {
    private GameModel gameModel;
    private HuntingEventFoodPointsBonus huntingEventFoodPointsBonus;
    private HuntingEvCard huntingEvCard;
    private ArtistsEvCard artistEvCard;
    private InventorCharCard inventorCharCard;
    private HunterCharCard hunterCharCard;
    private HunterCharCard hunterCharCard2;


    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        for (int i = 0; i < 2; i++) {
            gameModel.getPlayers().add(new Player("a", "r"));
        }
        gameModel.setCurrentPlayer(gameModel.getPlayers().getFirst());
        huntingEventFoodPointsBonus = new HuntingEventFoodPointsBonus(1, 1, 1, 1, 1);
        huntingEvCard = new HuntingEvCard(1, 1, 1, 2);
        artistEvCard = new ArtistsEvCard(1, 1, 1, 2, 1, 1);
        inventorCharCard = new InventorCharCard(1, 1, 1, "PANE");
        hunterCharCard = new HunterCharCard(1, 1, 1, true);
        hunterCharCard2 = new HunterCharCard(1, 1, 1, false);
    }

    @Test
    public void shouldAddFoodAndPoints() {
        gameModel.getPlayers().getFirst().getStats().getCharacterCards().add(inventorCharCard);
        gameModel.getPlayers().getFirst().getStats().getCharacterCards().add(hunterCharCard);

        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(hunterCharCard);
        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(hunterCharCard2);

        gameModel.getPlayers().getFirst().getStats().getBuildingCards().add(huntingEventFoodPointsBonus);
        gameModel.getPlayers().get(1).getStats().getBuildingCards().add(huntingEventFoodPointsBonus);

        for(Player p : gameModel.getPlayers()) {
            assertEquals(0, p.getStats().getFood());
            assertEquals(0, p.getStats().getPoints());
        }

        for(Player p : gameModel.getPlayers()) {
            for(BuildingCard b : p.getStats().getBuildingCards()) {
                b.onEventResolving(gameModel, huntingEvCard, p);
            }
        }

        assertEquals(1, gameModel.getPlayers().getFirst().getStats().getFood());
        assertEquals(1, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(2, gameModel.getPlayers().get(1).getStats().getFood());
        assertEquals(2, gameModel.getPlayers().get(1).getStats().getPoints());

    }

    @Test
    public void shouldNotAddFoodAndPoints() {
        gameModel.getPlayers().getFirst().getStats().getCharacterCards().add(inventorCharCard);

        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(hunterCharCard);
        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(hunterCharCard2);

        gameModel.getPlayers().getFirst().getStats().getBuildingCards().add(huntingEventFoodPointsBonus);

        for(Player p : gameModel.getPlayers()) {
            assertEquals(0, p.getStats().getFood());
            assertEquals(0, p.getStats().getPoints());
        }

        for(Player p : gameModel.getPlayers()) {
            for(BuildingCard b : p.getStats().getBuildingCards()) {
                b.onEventResolving(gameModel, huntingEvCard, p);
            }
        }

        for(Player p : gameModel.getPlayers()) {
            for(BuildingCard b : p.getStats().getBuildingCards()) {
                b.onEventResolving(gameModel, artistEvCard, p);
            }
        }

        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getFood());
        assertEquals(0, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(0, gameModel.getPlayers().get(1).getStats().getFood());
        assertEquals(0, gameModel.getPlayers().get(1).getStats().getPoints());

    }

}