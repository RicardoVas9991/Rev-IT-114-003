package Project.Common;

import java.util.ArrayList;
import java.util.List;

public class RoomResultsPayload extends Payload {
    private List<String> rooms = new ArrayList<>();

    public RoomResultsPayload() {
        super(PayloadType.ROOM_LIST);  // Call to super constructor
    }

    public List<String> getRooms() {
        return rooms;
    }

    public void setRooms(List<String> rooms) {
        this.rooms = rooms;
    }
}