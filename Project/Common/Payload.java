package Project.Common;
import java.io.Serializable;

public class Payload implements Serializable {
    private PayloadType payloadType;
    private long clientId;
    private String message;

    

    public PayloadType getPayloadType() {
        return payloadType;
    }



    public void setPayloadType(PayloadType payloadType) {
        this.payloadType = payloadType;
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

    private String target; // For private messaging - Rev/11-23-2024

    // Getter and Setter for target
    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }


    @Override
    public String toString(){
        return String.format("Payload[%s] Client Id [%s] Message: [%s]", getPayloadType(), getClientId(), getMessage());
    }
}