package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.card.buildingCard.ArtistEventFoodBonus;
import it.polimi.ingsw.am56.model.card.characterCard.ArtistCharCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArtistsEvCardTest {

    private GameModel gameModel;
    private ArtistsEvCard artistsEvCard;
    private ArtistCharCard artistCharCard;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();

        gameModel.getPlayers().add(new Player("a", "a"));
        gameModel.getPlayers().add(new Player("b", "b"));

        artistsEvCard = new ArtistsEvCard(1, 1, 1, 2, 10, 2);
        artistCharCard = new ArtistCharCard(1, 1, 1);
    }

    @Test
    void shouldLosePoints() {
        gameModel.getPlayers().getFirst().getStats().getCharacterCards().add(artistCharCard);

        artistsEvCard.resolveEffect(gameModel);

        assertEquals(-10, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(-10, gameModel.getPlayers().get(1).getStats().getPoints());
    }

    @Test
    void shouldWinPoints() {
        gameModel.getPlayers().getFirst().getStats().getCharacterCards().add(artistCharCard);
        gameModel.getPlayers().getFirst().getStats().getCharacterCards().add(artistCharCard);

        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(artistCharCard);
        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(artistCharCard);
        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(artistCharCard);

        artistsEvCard.resolveEffect(gameModel);

        assertEquals(4, gameModel.getPlayers().getFirst().getStats().getPoints());
        assertEquals(6, gameModel.getPlayers().get(1).getStats().getPoints());
    }

    @Test
    void shouldReturnCorrectEvType() {
        assertEquals(EvType.ARTIST, artistsEvCard.getEventType());
    }


    // test classe astratta EventCard


    @Test
    void shouldReturnCorrectCardType() {
        assertEquals(CardType.EVENT, artistsEvCard.getCardType());
    }

    @Test
    void shouldResolveBuildingsEffects() {
        ArtistEventFoodBonus artistEventFoodBonus = new ArtistEventFoodBonus(1, 1, 1, 1, 1);

        gameModel.setCurrentPlayer(gameModel.getPlayers().getFirst());
        artistCharCard.addCardToPlayer(gameModel.getCurrentPlayer());

        artistEventFoodBonus.addCardToPlayer(gameModel.getCurrentPlayer());

        assertEquals(0, gameModel.getCurrentPlayer().getStats().getFood());

        artistsEvCard.resolveEffect(gameModel);
        artistsEvCard.resolveBuildingsEffects(gameModel);

        assertEquals(1, gameModel.getCurrentPlayer().getStats().getFood());
    }
}