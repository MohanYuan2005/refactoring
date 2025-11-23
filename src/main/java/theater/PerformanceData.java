package theater;

/**
 * A data structure representing an enriched performance.
 */
public class PerformanceData {

    private final String playID;
    private final int audience;
    private final Play play;
    private final int amount;
    private final int volumeCredits;

    public PerformanceData(String playID,
                           int audience,
                           Play play,
                           int amount,
                           int volumeCredits) {

        this.playID = playID;
        this.audience = audience;
        this.play = play;
        this.amount = amount;
        this.volumeCredits = volumeCredits;
    }

    public String getPlayID() {
        return playID;
    }

    public int getAudience() {
        return audience;
    }

    public Play getPlay() {
        return play;
    }

    public int getAmount() {
        return amount;
    }

    public int getVolumeCredits() {
        return volumeCredits;
    }
}
