package it.polimi.ingsw.am56.model.card;

public class EventCard extends Card{
    private final evType type;
    private final int winPointsUpdateShamans;
    private final int losePointsUpdateShamans;
    private final int minArtists;
    private final int losePointsUpdateArtists;
    private final int winPointsUpdatePerArtist;
    private final int losePointsPerCharSustenance;
    private final int winPointsUpdatePerHunter;

    public EventCard(int era, int numPlayers, evType type, int winPointsUpdateShamans,
                     int losePointsUpdateShamans, int minArtists,
                     int losePointsUpdateArtists, int winPointsUpdatePerArtist,
                     int losePointsPerCharSustenance, int winPointsUpdatePerHunter) {
        super(era, numPlayers);
        this.type = type;
        this.winPointsUpdateShamans = winPointsUpdateShamans;
        this.losePointsUpdateShamans = losePointsUpdateShamans;
        this.minArtists = minArtists;
        this.losePointsUpdateArtists = losePointsUpdateArtists;
        this.winPointsUpdatePerArtist = winPointsUpdatePerArtist;
        this.losePointsPerCharSustenance = losePointsPerCharSustenance;
        this.winPointsUpdatePerHunter = winPointsUpdatePerHunter;
    }

    @Override
    public void resolveEffect() {}
}
