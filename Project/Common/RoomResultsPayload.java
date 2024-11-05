// rev/11-02-2024

package Project.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomResultsPayload extends Payload {
    private List<String> rooms = new ArrayList<>();
    private Map<String, Integer> playerPoints = new HashMap<>();

    public RoomResultsPayload() {
        setPayloadType(PayloadType.ROOM_LIST);
    }

    // For room management
    public List<String> getRooms() {
        return rooms;
    }

    public void setRooms(List<String> rooms) {
        this.rooms = rooms;
    }

    // For points syncing
    public Map<String, Integer> getPlayerPoints() {
        return playerPoints;
    }

    public void setPlayerPoints(Map<String, Integer> playerPoints) {
        this.playerPoints = playerPoints;
    }

    public void addPlayerPoint(String playerName, int points) {
        playerPoints.put(playerName, points);
    }
}