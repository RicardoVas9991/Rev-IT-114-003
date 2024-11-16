package Project.Common;

public class FlipPayload extends Payload {
    private String sender;
    private String result;

    public FlipPayload(String sender, String result) {
        super(PayloadType.FLIP);
        this.sender = sender;
        this.result = result;
    }

    public String getSender() { return sender; }
    public String getResult() { return result; }

    @Override
    public String toString() {
        return sender + " flipped a coin and got " + result;
    }
}
