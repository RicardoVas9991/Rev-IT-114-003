//rev - 11/13/2024
package Project.Common;

public class FlipPayload extends Payload {
    private String message;

    public FlipPayload() {
        setPayloadType(PayloadType.FLIP);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("Payload: " + getPayloadType(), "FlipPayload{" + "message='" + getMessage() + '\'' + '}');
    }
}
