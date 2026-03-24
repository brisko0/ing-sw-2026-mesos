package it.polimi.ingsw.am56.model;

public class Player {
    private final String nickname;
    private final String totemColor;
    private int pickedUpper;
    private int pickedLower;
    private final PlayerStats stats;


    public Player(String nickname, String color){
        this.nickname = nickname;
        this.totemColor = color;
        pickedUpper = 0;
        pickedLower = 0;
        this.stats = new PlayerStats();
    }

    public String getNickname() {
        return nickname;
    }

    public String getTotemColor() {
        return totemColor;
    }

    public int getPickedUpper() {
        return pickedUpper;
    }

    public int getPickedLower() {
        return pickedLower;
    }

    public PlayerStats getStats() {
        return stats;
    }

    public void setPickedUpper(int pickedUpper) {
        this.pickedUpper = pickedUpper;
    }

    public void setPickedLower(int pickedLower) {
        this.pickedLower = pickedLower;
    }
}
