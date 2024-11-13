//rev/11-11-2024
package Project.Common;

public class RollPayload extends Payload {
    private int dice;
    private int sides;

    public RollPayload(String sender, int dice, int sides) {
        super(PayloadType.ROLL);
        this.dice = dice;
        this.sides = sides;
    }

    public int getDice() { return dice; }
    public int getSides() { return sides; }

    @Override
public String toString() {
    return "RollPayload{" +
           "dice=" + dice +
           ", sides=" + sides +
           ", sender='" + getSender() + '\'' +
           '}';
}

}