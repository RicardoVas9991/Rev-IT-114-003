package Project.Common;

public class RollPayload extends Payload {
    private int dice;
    private int sides;

    public RollPayload(int dice, int sides) {
        this.dice = dice;
        this.sides = sides;
    }

    public int getDice() {
        return dice;
    }

    public int getSides() {
        return sides;
    }

    @Override
    public String toString() {
        return "ROLL:" + dice + "," + sides;
    }
}
