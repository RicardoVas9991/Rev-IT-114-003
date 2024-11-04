// rev/11-02-2024

package Project.Common;

import java.util.Map;

public class PointsPayload extends Payload {
    private Map<String, Integer> playerPoints;

    public PointsPayload(Map<String, Integer> playerPoints) {
        setPayloadType(PayloadType.POINT_UPDATE);
        this.playerPoints = playerPoints;
    }

    public Map<String, Integer> getPlayerPoints() {
        return playerPoints;
    }
}
