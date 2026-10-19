package it.polimi.ingsw.am56.model.card.buildingCard;

import it.polimi.ingsw.am56.model.GameModel;
import it.polimi.ingsw.am56.model.Player;
import it.polimi.ingsw.am56.model.card.characterCard.ArtistCharCard;
import it.polimi.ingsw.am56.model.card.characterCard.CharacterType;
import it.polimi.ingsw.am56.model.card.eventCard.ShamansEvCard;
import it.polimi.ingsw.am56.model.card.eventCard.SustenanceEvCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FeedingSaleCharTypeTest {

    private GameModel gameModel;
    private FeedingSaleCharType feedingSaleCharType;
    private DoubleBuildersPoints doubleBuildersPoints;
    private ArtistCharCard artistCharCard;
    private SustenanceEvCard sustenanceEvCard;
    private ShamansEvCard shamansEvCard;

    @BeforeEach
    void setUp() {
        gameModel = new GameModel();
        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            players.add(new Player("a", "r"));
            gameModel.getPlayers().add(players.get(i));
        }
        feedingSaleCharType = new FeedingSaleCharType(1, 1, 1, 1, 0, CharacterType.ARTIST);
        doubleBuildersPoints = new DoubleBuildersPoints(1, 1, 1, 1, 0);
        artistCharCard = new ArtistCharCard(1, 1, 1);
        sustenanceEvCard = new SustenanceEvCard(1, 1, 1, false, 1);
        shamansEvCard = new ShamansEvCard(1, 1, 1, false, 1, 1);
    }

    @Test
    void shouldApplyFeedingSale(){
        Player p1 = gameModel.getPlayers().getFirst();
        p1.getStats().getBuildingCards().add(feedingSaleCharType);
        p1.getStats().getCharacterCards().add(artistCharCard);
        p1.getStats().addFood(1);

        Player p2 = gameModel.getPlayers().get(1);
        p2.getStats().getBuildingCards().add(doubleBuildersPoints);
        p2.getStats().getCharacterCards().add(artistCharCard);
        p2.getStats().getCharacterCards().add(artistCharCard);
        p2.getStats().addFood(1);

        for (Player p : gameModel.getPlayers()) {
            assertEquals(1, p.getStats().getFood());
        }

        sustenanceEvCard.resolveEffect(gameModel);
        for (Player p : gameModel.getPlayers()) {
            for (BuildingCard b : p.getStats().getBuildingCards()) {
                b.onEventResolving(gameModel, sustenanceEvCard, p);
            }
        }

        assertEquals(1, p1.getStats().getFood());
        assertEquals(0, p2.getStats().getFood());
    }

    @Test
    void shouldNotApplyEffect(){
        Player p1 = gameModel.getPlayers().getFirst();
        p1.getStats().getBuildingCards().add(feedingSaleCharType);
        p1.getStats().getCharacterCards().add(artistCharCard);
        p1.getStats().addFood(1);

        assertEquals(1, p1.getStats().getFood());

        for(BuildingCard b : p1.getStats().getBuildingCards()){
            b.onEventResolving(gameModel, shamansEvCard, p1);
        }

        assertEquals(1, p1.getStats().getFood());
    }

}