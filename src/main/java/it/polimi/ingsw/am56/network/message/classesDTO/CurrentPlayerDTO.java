package it.polimi.ingsw.am56.network.message.classesDTO;

import java.io.Serializable;

public record CurrentPlayerDTO(String nickname, String color) implements Serializable {}