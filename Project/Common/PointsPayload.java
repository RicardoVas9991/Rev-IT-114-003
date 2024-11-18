// rev / 11-04-2024
package Project.Common;

import java.util.Map;

public class PointsPayload extends Payload {
    private Map<String, Integer> playerPoints;

    public PointsPayload(String clientId, Map<String, Integer> playerPoints) {
        super(clientId, "Points Update", "PointsPayload");
        this.playerPoints = playerPoints;
    }

    public Map<String, Integer> getPlayerPoints() {
        return playerPoints;
    }
}