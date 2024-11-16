package Project.Common;
import java.io.Serializable;

public class Payload implements Serializable {
    private PayloadType payloadType;
    private long clientId;
    private String message;
    public PayloadType type;
        
    public PayloadType getPayloadType() {
        return payloadType;
    }
        
        
    public void setPayloadType(PayloadType payloadType) {
        this.payloadType = payloadType;
    }
    
    public Payload() {
        this.type = PayloadType.DEFAULT; // Default value, if applicable
    }

    public Payload(PayloadType type) {
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
    public String toString(){
        return String.format("Payload[%s] Client Id [%s] Message: [%s]", getPayloadType(), getClientId(), getMessage());
    }
}