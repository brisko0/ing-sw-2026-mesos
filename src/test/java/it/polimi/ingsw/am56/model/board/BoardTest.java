package it.polimi.ingsw.am56.model.board;

import it.polimi.ingsw.am56.model.card.BuildingDeck;
import it.polimi.ingsw.am56.model.card.Card;
import it.polimi.ingsw.am56.model.card.CardType;
import it.polimi.ingsw.am56.model.card.Deck;
import it.polimi.ingsw.am56.model.card.buildingCard.FoodPerInventorPair;
import it.polimi.ingsw.am56.model.card.buildingCard.ShamanStarsBonus;
import it.polimi.ingsw.am56.model.card.characterCard.ArtistCharCard;
import it.polimi.ingsw.am56.model.card.characterCard.ShamanCharCard;
import it.polimi.ingsw.am56.model.factory.board.BoardFactory;
import it.polimi.ingsw.am56.model.factory.card.EventCardFactory;
import it.polimi.ingsw.am56.model.factory.deck.BuildingDeckFactory;
import it.polimi.ingsw.am56.model.factory.deck.CharacterDeckFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {
    private BoardFactory boardFactory;

    private CharacterDeckFactory characterDeckFactory;
    private EventCardFactory eventCardFactory;
    private BuildingDeckFactory buildingDeckFactory;

    private Deck characterDeck;
    private Deck eventDeck;
    private Deck charEvDeck;

    private BuildingDeck buildingDeck;

    private Board board;
    private int gameSize;

    @BeforeEach
    void setUp() {
        gameSize = 5;
        boardFactory = new BoardFactory();

        eventCardFactory = new EventCardFactory();

        characterDeckFactory = new CharacterDeckFactory();
        buildingDeckFactory = new BuildingDeckFactory();

        characterDeck = characterDeckFactory.createDeck(1, gameSize);

        charEvDeck = new Deck(1, gameSize, characterDeck.getRemainingCards());
        charEvDeck.getRemainingCards().addAll(eventCardFactory.createCards(1, gameSize));
        charEvDeck.shuffle();

        buildingDeck = buildingDeckFactory.createDeck(1, gameSize);

        board = boardFactory.createBoard(gameSize);
    }

    @Test
    void shouldFillLowerChar() {
        assertTrue(board.getLowerRowCharEv().isEmpty());

        board.fillLowerChar(gameSize, characterDeck);

        assertEquals(gameSize + 1, board.getLowerRowCharEv().size());
        for (Card card : board.getLowerRowCharEv()) {
            assertEquals(CardType.CHARACTER, card.getCardType());
        }

    }

    @Test
    void shouldFillEmptyUpperCharEv() {

        assertTrue(board.getUpperRowCharEv().isEmpty());

        board.fillUpperCharEv(gameSize, characterDeck);

        assertEquals(gameSize + 4, board.getUpperRowCharEv().size());
        for (Card card : board.getUpperRowCharEv()) {
            assertTrue(card.getCardType() == CardType.CHARACTER || card.getCardType() == CardType.EVENT);
        }

    }

    @Test
    void shouldFillNotEmptyUpperCharEv() {
        ShamanCharCard shamanCharCard = new ShamanCharCard(1, 1, 1, 1);
        board.getUpperRowCharEv().add(shamanCharCard);
        assertFalse(board.getUpperRowCharEv().isEmpty());

        board.fillUpperCharEv(gameSize, charEvDeck);

        assertEquals(gameSize + 4, board.getUpperRowCharEv().size());
        for (Card card : board.getUpperRowCharEv()) {
            assertTrue(card.getCardType() == CardType.CHARACTER || card.getCardType() == CardType.EVENT);
        }
    }

    @Test
    void shouldFillUntilDeckEnd() {
        assertTrue(board.getUpperRowCharEv().isEmpty());
        while(charEvDeck.getRemainingCards().size() > 4) {
            charEvDeck.drawCard();
        }

        board.fillUpperCharEv(gameSize, charEvDeck);

        assertEquals(4, board.getUpperRowCharEv().size());
        for (Card card : board.getUpperRowCharEv()) {
            assertTrue(card.getCardType() == CardType.CHARACTER || card.getCardType() == CardType.EVENT);
        }
    }

    @Test
    void shouldFillUpperBuildings() {
        int numBuildings = buildingDeck.getRemainingCards().size();
        assertTrue(board.getUpperRowBuildings().isEmpty());

        board.fillUpperBuildings(gameSize, buildingDeck);

        assertEquals(numBuildings, board.getUpperRowBuildings().size());
        for (Card card : board.getUpperRowBuildings()) {
            assertEquals(CardType.BUILDING, card.getCardType());
        }
    }

    @Test
    void shouldRemoveCard() {
        ShamanCharCard shamanCharCard = new ShamanCharCard(1, 1, 1, 1);
        ArtistCharCard artistCharCard = new ArtistCharCard(1, 1, 1);
        board.getUpperRowCharEv().add(shamanCharCard);
        board.getUpperRowCharEv().add(artistCharCard);

        FoodPerInventorPair foodPerInventorPair = new FoodPerInventorPair(1, 1, 1, 1, 1, 1);
        board.getLowerRowBuildings().add(foodPerInventorPair);

        board.removeCard(shamanCharCard);
        board.removeCard(foodPerInventorPair);

        assertEquals(1, board.getUpperRowCharEv().size());
        assertTrue(board.getUpperRowCharEv().contains(artistCharCard));
        assertTrue(board.getLowerRowBuildings().isEmpty());
    }

    @Test
    void shouldPerformRoundEndingActions() {
        ShamanCharCard shamanCharCard = new ShamanCharCard(1, 1, 1, 1);
        ArtistCharCard artistCharCard = new ArtistCharCard(1, 1, 1);
        board.getUpperRowCharEv().add(shamanCharCard);
        board.getUpperRowCharEv().add(artistCharCard);

        ShamanStarsBonus shamanStarsBonus = new ShamanStarsBonus(1, 1, 1, 1, 1, 1);
        board.getUpperRowBuildings().add(shamanStarsBonus);

        board.endRound(gameSize, charEvDeck);

        assertEquals(2, board.getLowerRowCharEv().size());
        assertTrue(board.getLowerRowCharEv().contains(shamanCharCard) &&  board.getLowerRowCharEv().contains(artistCharCard));

        assertEquals(gameSize + 4, board.getUpperRowCharEv().size());

        assertTrue(board.getUpperRowBuildings().contains(shamanStarsBonus));
        assertTrue(board.getLowerRowBuildings().isEmpty());
    }

    @Test
    void shouldPerformEraEndingActions() {
        ArtistCharCard artistCharCard = new ArtistCharCard(1, 1, 1);
        board.getUpperRowCharEv().add(artistCharCard);

        FoodPerInventorPair foodPerInventorPair = new FoodPerInventorPair(1, 1, 1, 1, 1, 1);
        board.getLowerRowBuildings().add(foodPerInventorPair);

        ShamanStarsBonus shamanStarsBonus = new ShamanStarsBonus(1, 1, 1, 1, 1, 1);
        board.getUpperRowBuildings().add(shamanStarsBonus);

        board.endEra(gameSize, charEvDeck, buildingDeck);

        assertEquals(gameSize + 4, board.getUpperRowCharEv().size());

        assertFalse(board.getUpperRowBuildings().isEmpty());

        assertEquals(1, board.getLowerRowBuildings().size());
        assertTrue(board.getLowerRowBuildings().contains(shamanStarsBonus));
    }

    @Test
    void shouldReturnCorrectCard() {
        ArtistCharCard artistCharCard = new ArtistCharCard(1, 1, 1);
        ShamanCharCard shamanCharCard = new ShamanCharCard(2, 1, 1, 1);
        board.getUpperRowCharEv().add(artistCharCard);
        board.getUpperRowCharEv().add(shamanCharCard);

        Card c = board.getCardById(2);

        assertEquals(c, shamanCharCard);

    }


}