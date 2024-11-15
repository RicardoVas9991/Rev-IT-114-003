//rev/11-11-2024
package Project.Common;

public class RollPayload extends Payload {
    private int dice;
    private int sides;
    private String message;

    public RollPayload() {
        setPayloadType(PayloadType.ROLL);
    }

    public int getDice() { 
        return dice; 
    }

    public void setDice(int dice) { // rev/11-14-2024
        this.dice = dice;
    }

    public int getSides() { 
        return sides; 
    }

    public void setSide(int sides) {
        this.sides = sides;
    }

    public String getMessage() { // rev/11-14-2024
        return message;
    }

    public void setMessage(String message) { // rev/11-14-2024
        this.message = message;
    }


    @Override
    public String toString() {
        return String.format("Payload: " + getPayloadType(), "RollPayload{" + "dice=" +  getDice() + ", sides=" + getSides() + ", message='" + getMessage() + '\'' + '}');
    }

}