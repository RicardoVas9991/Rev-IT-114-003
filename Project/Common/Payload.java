package Project.Common;
import java.io.Serializable;

public class Payload implements Serializable {
    private PayloadType payloadType;
    private long clientId;
    private String message;
    private String command;
    private String sender;
    private PayloadType type;

    public Payload(PayloadType type) {
        this.type = type;
    }

    public PayloadType getType() { return type; }
    public String getSender() { return sender; }

    public Payload(String command, String sender) {
        this.command = command;
        this.sender = sender;
    }

    public String getCommand() { return command; }

    

    public PayloadType getPayloadType() {
        return payloadType;
    }



    public void setPayloadType(PayloadType type) {
        this.type = type;
    }



    public long getClientId() {
        return clientId;
    }



    public void setClientId(long clientId) {
        this.clientId = clientId;
    }



    public String getMessage() {
        return message;
    }



    public void setMessage(String message) {
        this.message = message;
    }



    @Override
    public String toString() {
        return "Payload Type: " + type;
    }
}