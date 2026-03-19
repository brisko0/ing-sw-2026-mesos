package it.polimi.ingsw.am56.model.player;

public class Player {
    private final String nickname;
    private final String totemColor;
    private PlayerStats stats;

    public Player(String nickname, String color){
        this.nickname = nickname;
        this.totemColor = color;
        this.stats = new PlayerStats();
    }

    public PlayerStats getStats() {
        return stats;
    }

    public String getNickname() {
        return nickname;
    }

    public String getTotemColor() {
        return totemColor;
    }
}
