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
    public static FruitGameRules getFruitGraphics(String gameName) {
        FruitGameRules rules = null;

        if (gameName.equals("Gates of Olympus")) {
            rules = new FruitGameRules(R.drawable.gates_of_olympus_logo, new String[]{"⚡"}, Color.parseColor("#EDD606"));
            rules.addWinPattern(40.0, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
            rules.addWinPattern(6.5, new int[]{0, 2, 4, 6, 8});
            rules.addWinPattern(2.0, new int[]{1, 3, 4, 5, 7});
            rules.addWinPattern(1.0, new int[]{3, 4, 5});

        } else if (gameName.equals("Fruit Fiesta")) {
            rules = new FruitGameRules(R.drawable.fruit_fiesta_logo, new String[]{"🍒", "🍋", "🍉", "🍇", "🍓"}, Color.parseColor("#FFFFFF"));
            rules.addWinPattern(10.0, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
            rules.addWinPattern(2.5, new int[]{0, 2, 4, 6, 8});
            rules.addWinPattern(2.0, new int[]{1, 3, 4, 5, 7});
            rules.addWinPattern(1.3, new int[]{1, 4, 7});
            rules.addWinPattern(1.1, new int[]{0, 1, 2});
            rules.addWinPattern(1.0, new int[]{3, 4, 5});
            rules.addWinPattern(0.5, new int[]{3, 5});
            rules.addWinPattern(0.1, new int[]{4});

        } else if (gameName.equals("Lucky Seven")) {
            rules = new FruitGameRules(R.drawable.lucky_seven_logo, new String[]{"7"}, Color.parseColor("#E70614"));
            rules.addWinPattern(10.0, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
            rules.addWinPattern(2.5, new int[]{0, 2, 4, 6, 8});
            rules.addWinPattern(2.0, new int[]{1, 3, 4, 5, 7});
            rules.addWinPattern(1.3, new int[]{1, 4, 7});
            rules.addWinPattern(1.1, new int[]{0, 1, 2});
            rules.addWinPattern(1.0, new int[]{3, 4, 5});
            rules.addWinPattern(0.5, new int[]{3, 5});
            rules.addWinPattern(0.1, new int[]{4});
        }

        return rules;
    }
    public HashMap<Double, int[]> getWinPatterns() { return winPatterns; }
    public int getLogoResId() { return logoResId; }
    public int getColor() { return color; }
    public String[] getSymbols() { return symbols; }

}