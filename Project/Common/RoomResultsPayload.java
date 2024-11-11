// rev / 11-04-2024

package Project.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomResultsPayload extends Payload {
    private Map<String, Integer> playerPoints;

    public RoomResultsPayload(Map<String, Integer> playerPoints) {
        super(PayloadType.POINT_UPDATE);
        this.playerPoints = playerPoints;
    }

    public Map<String, Integer> getPlayerPoints() {
        return playerPoints;
    }
}