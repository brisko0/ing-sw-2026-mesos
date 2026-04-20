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
}