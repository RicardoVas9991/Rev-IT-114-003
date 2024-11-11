package Project.Common;

public class FlipPayload extends Payload {
    public FlipPayload(String sender) {
        super(PayloadType.FLIP, sender);
    }

    @Override
    public String toString() {
        return "FlipPayload{" +
               "sender='" + getSender() + '\'' +
               '}';
    }

}