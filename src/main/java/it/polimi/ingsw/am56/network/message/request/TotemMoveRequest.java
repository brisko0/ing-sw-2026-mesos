package it.polimi.ingsw.am56.network.message.request;

import java.io.Serializable;

public record TotemMoveRequest(String nickname, String offerTileLetter) implements Serializable {}