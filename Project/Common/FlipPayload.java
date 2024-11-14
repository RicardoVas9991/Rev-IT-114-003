//rev - 11/13/2024
package Project.Common;

public class FlipPayload extends Payload {
    private String sender;

    public FlipPayload(String sender) {
        super(PayloadType.FLIP);
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }

    @Override
    public String toString() {
        return "FlipPayload{" +
               "sender='" + sender + '\'' +
               '}';
    }
}
