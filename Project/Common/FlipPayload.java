package Project.Common;
import java.lang.Math;

public class FlipPayload extends Payload {
    private String sender;
    private String result;

    public FlipPayload(String sender) {
        super(PayloadType.FLIP);
        this.sender = sender;
        this.result = Math.random() < 0.5 ? "heads" : "tails";
    }

    public String getSender() { 
        return sender; 
    }
    
    public String getResult() { 
        return result; 
    }

    @Override
    public String toString() {
        return sender + " flipped a coin and got " + result;
    }
}
