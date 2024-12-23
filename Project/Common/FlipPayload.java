package Project.Common;
import java.lang.Math; // - Rev/11/-16-2024

public class FlipPayload extends Payload {
    private String sender;
    private String result;

    public FlipPayload(String sender) {
        super();
        this.sender = sender;
        this.result = Math.random() < 0.5 ? "heads" : "tails"; // - Rev/11/-16-2024
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