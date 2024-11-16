package Project.Common;


public class RollPayload extends Payload {
    private int dice;
    private int sides;
    private String sender;

    public RollPayload(String sender, int dice, int sides) {
        super(PayloadType.ROLL);
        this.sender = sender;
        this.dice = dice;
        this.sides = sides;
    }

    public int getDice() { return dice; }
    public int getSides() { return sides; }
    public String getSender() { return sender; }

    @Override
    public String toString() {
        return sender + " rolled " + dice + "d" + sides;
    }
}
