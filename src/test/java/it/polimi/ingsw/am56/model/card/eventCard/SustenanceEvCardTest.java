package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.ArtistCharCard;
import it.polimi.ingsw.am56.model.card.characterCard.CollectorCharCard;
import it.polimi.ingsw.am56.model.card.characterCard.HunterCharCard;
import it.polimi.ingsw.am56.model.card.characterCard.ShamanCharCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SustenanceEvCardTest {

    private GameModel gameModel;
    private SustenanceEvCard sustenanceEvCard;
    private ArtistCharCard artistCharCard;
    private ShamanCharCard shamanCharCard;
    private HunterCharCard hunterCharCard;
    private CollectorCharCard collectorCharCard;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        for (int i = 0; i < 3; i++) {
            gameModel.getPlayers().add(new Player("a", "a"));
        }
        sustenanceEvCard = new SustenanceEvCard(1, 1, 1, false, 2);
        artistCharCard = new ArtistCharCard(1, 1, 1);
        hunterCharCard = new HunterCharCard(1, 1, 1, true);
        shamanCharCard = new ShamanCharCard(1, 1, 1, 2);
        collectorCharCard = new CollectorCharCard(1, 1, 1, 3);
    }

    @Test
    void shouldLosePoints() {
        for (int i = 0; i < 3; i++) {
            gameModel.getPlayers().get(i).getStats().getCharacterCards().add(artistCharCard);
            gameModel.getPlayers().get(i).getStats().getCharacterCards().add(hunterCharCard);
            gameModel.getPlayers().get(i).getStats().getCharacterCards().add(shamanCharCard);
            gameModel.getPlayers().get(i).getStats().addFood(i);
        }

        sustenanceEvCard.resolveEffect(gameModel);

        for(int i = 0; i < 3; i++) {
            assertEquals(0, gameModel.getPlayers().get(i).getStats().getFood());
        }

        assertEquals(-6, gameModel.getPlayers().get(0).getStats().getPoints());
        assertEquals(-4, gameModel.getPlayers().get(1).getStats().getPoints());
        assertEquals(-2, gameModel.getPlayers().get(2).getStats().getPoints());

    }

    @Test
    void checkFeedingSale() {
        for (int i = 0; i < 3; i++) {
            gameModel.getPlayers().get(i).getStats().getCharacterCards().add(artistCharCard);
            gameModel.getPlayers().get(i).getStats().getCharacterCards().add(hunterCharCard);
            gameModel.getPlayers().get(i).getStats().getCharacterCards().add(shamanCharCard);
            gameModel.getPlayers().get(i).getStats().getCharacterCards().add(collectorCharCard);
            gameModel.getPlayers().get(i).getStats().addFood(i);
            gameModel.getPlayers().get(i).getStats().addFeedingSale(3);
        }

        sustenanceEvCard.resolveEffect(gameModel);

        assertEquals(0, gameModel.getPlayers().get(0).getStats().getFood());
        assertEquals(0, gameModel.getPlayers().get(1).getStats().getFood());
        assertEquals(1, gameModel.getPlayers().get(2).getStats().getFood());

        assertEquals(-2, gameModel.getPlayers().get(0).getStats().getPoints());
        assertEquals(0, gameModel.getPlayers().get(1).getStats().getPoints());
        assertEquals(0, gameModel.getPlayers().get(2).getStats().getPoints());
    }

    @Test
    void shouldReturnCorrectEvType() {
        assertEquals(EvType.SUSTENANCE, sustenanceEvCard.getEventType());
    }



}