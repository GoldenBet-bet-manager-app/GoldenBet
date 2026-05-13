package gr.goldenbet.domain;
import java.util.HashMap;



public class FruitGameRules {


    private String[] symbols;
    private int color;

    private HashMap<Double, int[]> winPatterns = new HashMap<>();

    public FruitGameRules( String[] symbols, int color) {

        this.symbols = symbols;
        this.color = color;
    }

    public void addWinPattern(Double multiplier, int[] indices) {
        this.winPatterns.put(multiplier, indices);
    }

    public HashMap<Double, int[]> getWinPatterns() { return winPatterns; }
    public int getColor() { return color; }
    public String[] getSymbols() { return symbols; }

}