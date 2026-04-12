package it.polimi.ingsw.am56.model.card.eventCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.ArtistCharCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArtistsEvCardTest {

    private GameModel gameModel;
    private Player p1;
    private Player p2;
    private ArtistsEvCard artistsEvCard;
    private ArtistCharCard artistCharCard;

    @BeforeEach
    void setUp() {
        p1 = new Player("a", "r");
        p2 = new Player("b", "b");
        gameModel = new GameModel();
        artistsEvCard = new ArtistsEvCard(1, 1, 1, 2, 10, 2);
        artistCharCard = new ArtistCharCard(1, 1, 1);
        gameModel.getPlayers().add(p1);
        gameModel.getPlayers().add(p2);
    }

    @Test
    void shouldLosePoints() {
        p1.getStats().getCharacterCards().add(artistCharCard);

        artistsEvCard.resolveEffect(gameModel);

        assertEquals(-10, p1.getStats().getPoints());
        assertEquals(-10, p2.getStats().getPoints());
    }

    @Test
    void shouldWinPoints() {
        p1.getStats().getCharacterCards().add(artistCharCard);
        p1.getStats().getCharacterCards().add(artistCharCard);

        p2.getStats().getCharacterCards().add(artistCharCard);
        p2.getStats().getCharacterCards().add(artistCharCard);
        p2.getStats().getCharacterCards().add(artistCharCard);

        artistsEvCard.resolveEffect(gameModel);

        assertEquals(4, p1.getStats().getPoints());
        assertEquals(6, p2.getStats().getPoints());
    }

    @Test
    void shouldReturnCorrectEvType() {
        assertEquals(EvType.ARTIST, artistsEvCard.getEventType());
    }
}