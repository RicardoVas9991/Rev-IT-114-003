package Project.Common;

public class PrivateMessagePayload extends Payload {
    private String senderId;
    private String targetId;
    private String message;

    public PrivateMessagePayload(String senderId, String targetId, String message) {
        this.senderId = senderId;
        this.targetId = targetId;
        this.message = message;
        setPayloadType(PayloadType.PRIVATE_MESSAGE); // Assign the new type
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "PrivateMessagePayload{" +
               "senderId='" + senderId + '\'' +
               ", targetId='" + targetId + '\'' +
               ", message='" + message + '\'' +
               '}';
    }
}
