package Project.Common;

/**
 * Common Player data shared between Client and Server
 */
public class Player {
    public static long DEFAULT_CLIENT_ID = -1L;
    private long clientId = Player.DEFAULT_CLIENT_ID;
    private boolean isReady = false;
    private int score = 0; // ucid/date (rev/11-02-2024): Added score attribute to Player class

    public long getClientId() {
        return clientId;
    }
    
    public void setClientId(long clientId2) {
        this.clientId = clientId2;
    }

    public boolean isReady() {
        return isReady;
    }
    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    // ucid/date (rev/11-02-2024): Added score getter and setter
    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
    
    /**
     * Resets all of the data (this is destructive).
     * You may want to make a softer reset for other data
     */
    public void reset() {
        this.clientId = Player.DEFAULT_CLIENT_ID;
        this.isReady = false;
        this.score = 0; // ucid/date (rev/11-02-2024): Reset score in reset method
    }
}
