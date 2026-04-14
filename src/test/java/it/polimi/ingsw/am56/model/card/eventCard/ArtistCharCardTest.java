package it.polimi.ingsw.am56.model.card.characterCard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArtistCharCardTest {

    private ArtistCharCard artistCharCard;

    @BeforeEach
    void setUp() {
        artistCharCard = new ArtistCharCard(1, 1, 1);
    }

    @Test
    void shouldReturnCorrectCharType() {
        assertEquals(CharacterType.ARTIST,  artistCharCard.getCharacterType());
    }

}