package gr.goldenbet.domain;

import android.graphics.Color;
import java.util.HashMap;
import gr.goldenbet.R;

public class GameRules {// na to spaso se dio klaseis!!!!!!!!!!11

    // ==========================================
    // 1. ΜΕΤΑΒΛΗΤΕΣ ΤΟΥ ΠΑΙΧΝΙΔΙΟΥ
    // ==========================================
    private int logoResId;
    private String[] symbols;
    private String defaultSymbol;
    private int color;
    private HashMap<Double, int[]> winPatterns = new HashMap<>();

    // ==========================================
    // 2. CONSTRUCTOR (Φτιάχνει το βασικό κουτί)
    // ==========================================
    public GameRules(int logoResId, String[] symbols, String defaultSymbol, int color) {
        this.logoResId = logoResId;
        this.color = color;
        this.defaultSymbol = defaultSymbol;
        this.symbols = symbols;
    }

    // ==========================================
    // 3. ΕΡΓΟΣΤΑΣΙΟ ΓΙΑ ΦΡΟΥΤΑΚΙΑ
    // ==========================================
    public static GameRules getFruitGraphics(String gameName) {
        GameRules rules;

        if (gameName.equals("Gates of Olympus")) {
            rules = new GameRules(R.drawable.gates_of_olympus_logo, new String[]{"⚡", " "}, "⚡", Color.parseColor("#EDD606"));
            rules.winPatterns.put(40.0, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
            rules.winPatterns.put(6.5, new int[]{0, 2, 4, 6, 8});
            rules.winPatterns.put(2.0, new int[]{1, 3, 4, 5, 7});
            rules.winPatterns.put(1.0, new int[]{3, 4, 5});
            return rules;

        } else if (gameName.equals("Fruit Fiesta")) {
            rules = new GameRules(R.drawable.fruit_fiesta_logo, new String[]{"🍒", "🍋", "🍉", "🍇", "🍓"}, "χ", Color.parseColor("#FFFFFF"));
            rules.winPatterns.put(10.0, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
            rules.winPatterns.put(2.5, new int[]{0, 2, 4, 6, 8});
            rules.winPatterns.put(2.0, new int[]{1, 3, 4, 5, 7});
            rules.winPatterns.put(1.3, new int[]{1, 4, 7});
            rules.winPatterns.put(1.1, new int[]{0, 1, 2});
            rules.winPatterns.put(1.0, new int[]{3, 4, 5});
            rules.winPatterns.put(0.5, new int[]{3, 5});
            rules.winPatterns.put(0.1, new int[]{4});
            return rules;

        } else if (gameName.equals("Lucky Seven")) {
            rules = new GameRules(R.drawable.lucky_seven_logo, new String[]{"7", " "}, "7", Color.parseColor("#E70614"));
            rules.winPatterns.put(10.0, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
            rules.winPatterns.put(2.5, new int[]{0, 2, 4, 6, 8});
            rules.winPatterns.put(2.0, new int[]{1, 3, 4, 5, 7});
            rules.winPatterns.put(1.3, new int[]{1, 4, 7});
            rules.winPatterns.put(1.1, new int[]{0, 1, 2});
            rules.winPatterns.put(1.0, new int[]{3, 4, 5});
            rules.winPatterns.put(0.5, new int[]{3, 5});
            rules.winPatterns.put(0.1, new int[]{4});
            return rules;
        }

        return null;
    }


    public static GameRules getWheelGraphics(String gameName) {

        if (gameName.equals("Pharaoh's Dynasty")) {
            return new GameRules(R.drawable.pharaohs_dynasty_logo, new String[]{}, "", 0);

        } else if (gameName.equals("Dragon's Lair")) {
            return new GameRules(R.drawable.dragons_lair_logo, new String[]{}, "", 0);

        } else if (gameName.equals("Pirate's Bounty")) {
            return new GameRules(R.drawable.pirates_bounty_logo, new String[]{}, "", 0);
        }

        return null;
    }

    public HashMap<Double, int[]> getWinPatterns() { return winPatterns; }
    public int getLogoResId() { return logoResId; }
    public int getColor() { return color; }
    public String getDefaultSymbol() { return defaultSymbol; }
    public String[] getSymbols() { return symbols; }
}