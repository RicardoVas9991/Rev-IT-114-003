package Project.Common;
import java.io.Serializable;

public class Payload implements Serializable {
    private PayloadType payloadType; // Consolidated field for payload type
    private long clientId;
    private String message;

    // Default Constructor
    public Payload() {
        this.payloadType = PayloadType.DEFAULT; // Ensure a default value
    }

    // Parameterized Constructor
    public Payload(PayloadType payloadType) {
        if (payloadType == null) {
            throw new IllegalArgumentException("PayloadType cannot be null");
        }
        this.payloadType = payloadType;
    }

    // Getter and Setter for PayloadType
    public PayloadType getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(PayloadType payloadType) {
        if (payloadType == null) {
            throw new IllegalArgumentException("PayloadType cannot be null");
        }
        this.payloadType = payloadType;
    }

    // Getter and Setter for Client ID
    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    // Getter and Setter for Message
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // String Representation
    @Override
    public String toString() {
        return String.format("Payload[%s] Client Id [%s] Message: [%s]", getPayloadType(), getClientId(), getMessage());
    }
}
