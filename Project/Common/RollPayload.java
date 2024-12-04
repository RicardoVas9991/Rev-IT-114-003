package Project.Common;

public class RollPayload extends Payload {
    private int dice;
    private int sides;
    private int total;

    public RollPayload(int dice, int sides, int total) {
        this.dice = dice;
        this.sides = sides;
        this.total = total;
    }

    public int getDice() {
        return dice;
    }

    public int getSides() {
        return sides;
    }
    
    public int getTotal() { 
        return total; 
    }

    @Override
    public String toString() {
        return "ROLL:" + dice + "," + sides + " and got a " + total;
    }
}
