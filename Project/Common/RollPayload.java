package Project.Common;

public class RollPayload extends Payload {
    private int dice;
    private int sides;
    private String sender;
    private int total;

    public RollPayload(String sender, int dice, int sides, int total) {
        super();
        this.sender = sender;
        this.dice = dice;
        this.sides = sides;
        this.total = total;
    }

    public int getDice() { return dice; }
    public int getSides() { return sides; }
    public String getSender() { return sender; }
    public int getTotal() { return total; }

    @Override
    public String toString() {
        return sender + " rolled " + dice + "d" + sides + " and got " + total;
    }
}
