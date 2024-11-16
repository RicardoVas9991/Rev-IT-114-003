package Project.Common;

import java.util.ArrayList;
import java.util.List;

public class RoomResultsPayload extends Payload {
    private List<String> rooms = new ArrayList<String>();

    public RoomResultsPayload(PayloadType type) {
        super(type); // Call to the Payload constructor
    }

    public List<String> getRooms() {
        return rooms;
    }

    public void setRooms(List<String> rooms) {
        this.rooms = rooms;
    }
}