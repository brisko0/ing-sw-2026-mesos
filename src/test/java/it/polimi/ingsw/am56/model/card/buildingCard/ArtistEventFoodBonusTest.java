package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;
import it.polimi.ingsw.am56.model.card.eventCard.ArtistsEvCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArtistEventFoodBonusTest {

    private GameModel gameModel;
    private AddPointsEndGame addPointsEndGame;
    private ArtistEventFoodBonus artistEventFoodBonus;
    private ArtistsEvCard artistsEvCard;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            players.add(new Player("a", "r"));
            gameModel.getPlayers().add(players.get(i));
        }
        addPointsEndGame = new AddPointsEndGame(1, 1, 1, 1, 0, 25);
        artistEventFoodBonus = new ArtistEventFoodBonus(1, 1, 1, 1, 0);
        artistsEvCard = new ArtistsEvCard(1, 1, 1, 1, 1, 1);
    }

    @Test
    void shouldAddFoodBonusIfCardOwned(){
        gameModel.getPlayers().get(0).getStats().getBuildingCards().add(artistEventFoodBonus);
        gameModel.getPlayers().get(1).getStats().getBuildingCards().add(addPointsEndGame);

        for (Player p : gameModel.getPlayers()) {
            assertEquals(0, p.getStats().getFood());
        }

        for (Player p : gameModel.getPlayers()) {
            for (BuildingCard b : p.getStats().getBuildingCards()) {
                b.onEventResolving(gameModel, artistsEvCard, p);
            }
        }

        assertEquals(gameModel.getPlayers().get(0).getStats().getCharacterTypeNum(CharacterType.ARTIST), gameModel.getPlayers().get(0).getStats().getFood());
        assertEquals(0, gameModel.getPlayers().get(1).getStats().getFood());


    }

}