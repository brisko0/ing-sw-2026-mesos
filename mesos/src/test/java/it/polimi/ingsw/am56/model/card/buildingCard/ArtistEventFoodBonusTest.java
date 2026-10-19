package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.ArtistCharCard;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;
import it.polimi.ingsw.am56.model.card.eventCard.ArtistsEvCard;
import it.polimi.ingsw.am56.model.card.eventCard.ShamansEvCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArtistEventFoodBonusTest {

    private GameModel gameModel;
    private AddPointsEndGame addPointsEndGame;
    private ArtistEventFoodBonus artistEventFoodBonus;
    private ArtistCharCard artistCharCard;
    private ArtistsEvCard artistsEvCard;
    private ShamansEvCard shamansEvCard;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            players.add(new Player("a", "r"));
            gameModel.getPlayers().add(players.get(i));
        }
        artistCharCard = new ArtistCharCard(1, 1, 1);
        addPointsEndGame = new AddPointsEndGame(1, 1, 1, 1, 0, 25);
        artistEventFoodBonus = new ArtistEventFoodBonus(1, 1, 1, 1, 0);
        artistsEvCard = new ArtistsEvCard(1, 1, 1, false, 1, 1, 1);
        shamansEvCard = new ShamansEvCard(1, 1, 1, false, 1, 1);
    }

    @Test
    void shouldAddFoodBonusIfCardOwned(){
        gameModel.getPlayers().getFirst().getStats().getBuildingCards().add(artistEventFoodBonus);
        gameModel.getPlayers().getFirst().getStats().getCharacterCards().add(artistCharCard);

        gameModel.getPlayers().get(1).getStats().getBuildingCards().add(addPointsEndGame);
        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(artistCharCard);

        for (Player p : gameModel.getPlayers()) {
            assertEquals(0, p.getStats().getFood());
        }

        for (Player p : gameModel.getPlayers()) {
            for (BuildingCard b : p.getStats().getBuildingCards()) {
                b.onEventResolving(gameModel, artistsEvCard, p);
            }
        }

        assertEquals(gameModel.getPlayers().getFirst().getStats().getCharacterTypeNum(CharacterType.ARTIST), gameModel.getPlayers().getFirst().getStats().getFood());
        assertEquals(0, gameModel.getPlayers().get(1).getStats().getFood());


    }
    @Test
    void shouldNotResolve() {
        gameModel.getPlayers().getFirst().getStats().getBuildingCards().add(artistEventFoodBonus);
        gameModel.getPlayers().getFirst().getStats().getCharacterCards().add(artistCharCard);

        gameModel.getPlayers().get(1).getStats().getBuildingCards().add(addPointsEndGame);
        gameModel.getPlayers().get(1).getStats().getCharacterCards().add(artistCharCard);

        for (Player p : gameModel.getPlayers()) {
            assertEquals(0, p.getStats().getFood());
        }

        for (Player p : gameModel.getPlayers()) {
            for (BuildingCard b : p.getStats().getBuildingCards()) {
                b.onEventResolving(gameModel, shamansEvCard, p);
            }
        }

        for (Player p : gameModel.getPlayers()) {
            assertEquals(0, p.getStats().getFood());
        }
    }

}