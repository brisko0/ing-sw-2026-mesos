package it.polimi.ingsw.am56.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player p;

    @BeforeEach
    void setUp() {
        p = new Player("carlo", "rosso");
    }

    @Test
    void shouldReturnCorrectNickname() {
        assertEquals("carlo", p.getNickname());
    }

    @Test
    void shouldReturnCorrectTotemColor() {
        assertEquals("rosso", p.getTotemColor());
    }

    @Test
    void pickedUpperTest() {
        assertEquals(0, p.getPickedUpper());
        p.setPickedUpper(2);
        assertEquals(2, p.getPickedUpper());
    }

    @Test
    void pickedLowerTest() {
        assertEquals(0, p.getPickedLower());
        p.setPickedLower(3);
        assertEquals(3, p.getPickedLower());
    }

}