package gr.goldenbet.domain;

import android.graphics.Color;

import java.util.HashMap;

import gr.goldenbet.R;

public class FruitGameRules {

    private int logoResId;
    private String[] symbols;
    private int color;

    private HashMap<Double, int[]> winPatterns = new HashMap<>();

    public FruitGameRules(int logoResId, String[] symbols, int color) {
        this.logoResId = logoResId;
        this.symbols = symbols;
        this.color = color;
    }

    public void addWinPattern(Double multiplier, int[] indices) {
        this.winPatterns.put(multiplier, indices);
    }

    public HashMap<Double, int[]> getWinPatterns() { return winPatterns; }
    public int getLogoResId() { return logoResId; }
    public int getColor() { return color; }
    public String[] getSymbols() { return symbols; }

}