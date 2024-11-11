package Project.Common;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Payload implements Serializable {
    private String clientId;
    private String message;
    private String type;

    public Payload(String clientId, String message, String type) {
        this.clientId = clientId;
        this.message = message;
        this.type = type;
    }

    public Payload(String room, String room2, String room3) {
        //TODO Auto-generated constructor stub
    }

    public Payload(PayloadType question) {
        //TODO Auto-generated constructor stub
    }

    public String getClientId() {
        return clientId;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public void setPayloadType(PayloadType roomList) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setPayloadType'");
    }

    public void setMessage(String roomQuery) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setMessage'");
    }
}