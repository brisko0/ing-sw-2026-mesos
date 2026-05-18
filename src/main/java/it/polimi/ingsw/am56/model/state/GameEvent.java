package it.polimi.ingsw.am56.model.state;

public enum GameEvent {
    LOBBY_STATE_SYNC,

    PLAYER_ADDED,
    GAME_STARTED,
    STATE_CHANGED,
    TURN_CHANGED,
    TOTEM_PLACED,
    CARD_PICKED,
    TOTEM_RESET,
    ROUND_ENDED,
    ERA_CHANGED,
    GAME_ENDED,
    EVENT_RESOLVED,
}