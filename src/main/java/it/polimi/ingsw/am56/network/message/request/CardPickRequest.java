package it.polimi.ingsw.am56.network.message.request;

import java.io.Serializable;

public record CardPickRequest(String nickname, int cardId) implements Serializable {}