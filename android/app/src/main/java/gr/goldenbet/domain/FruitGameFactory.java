package gr.goldenbet.domain;

import android.graphics.Color;

public class FruitGameFactory {

    public FruitGameFactory() {
    }

    public FruitGameRules getFruitGraphics(String gameName) {
        FruitGameRules rules = null;

        if (gameName == null) {
            return null;
        }

        if (gameName.equals("Gates of Olympus")) {
            rules = new FruitGameRules(new String[]{"⚡", "💎", "👑", "💍", "🏆", "⏳"}, Color.parseColor("#EDD606"));
            rules.addWinPattern(40.0, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
            rules.addWinPattern(6.5, new int[]{0, 2, 4, 6, 8});
            rules.addWinPattern(2.0, new int[]{1, 3, 4, 5, 7});
            rules.addWinPattern(1.0, new int[]{3, 4, 5});

        } else if (gameName.equals("Fruit Fiesta")) {
            rules = new FruitGameRules(new String[]{"🍋", "🍒", "🍉", "🍇", "🍓"}, Color.parseColor("#FFFFFF"));
            rules.addWinPattern(10.0, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
            rules.addWinPattern(2.5, new int[]{0, 2, 4, 6, 8});
            rules.addWinPattern(2.0, new int[]{1, 3, 4, 5, 7});
            rules.addWinPattern(1.3, new int[]{1, 4, 7});
            rules.addWinPattern(1.1, new int[]{0, 1, 2});
            rules.addWinPattern(1.0, new int[]{3, 4, 5});
            rules.addWinPattern(0.5, new int[]{3, 5});
            rules.addWinPattern(0.1, new int[]{4});

        } else if (gameName.equals("Lucky Seven")) {
            rules = new FruitGameRules(new String[]{"7", "🍒", "🔔", "⭐", "🎲", "🍀"}, Color.parseColor("#E70614"));
            rules.addWinPattern(10.0, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
            rules.addWinPattern(2.5, new int[]{0, 2, 4, 6, 8});
            rules.addWinPattern(2.0, new int[]{1, 3, 4, 5, 7});
            rules.addWinPattern(1.3, new int[]{1, 4, 7});
            rules.addWinPattern(1.1, new int[]{0, 1, 2});
            rules.addWinPattern(1.0, new int[]{3, 4, 5});
            rules.addWinPattern(0.5, new int[]{3, 5});
            rules.addWinPattern(0.1, new int[]{4});

        } else {
            rules = new FruitGameRules(new String[]{"10", "🔔", "⭐", "🍇", "🍊", "💎"}, Color.parseColor("#FFEB3B"));

            rules.addWinPattern(40.0, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
            rules.addWinPattern(20.0, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
            rules.addWinPattern(10.0, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});

            rules.addWinPattern(6.5, new int[]{0, 2, 4, 6, 8});
            rules.addWinPattern(3.5, new int[]{0, 2, 4, 6, 8});
            rules.addWinPattern(2.5, new int[]{1, 3, 4, 5, 7});

            rules.addWinPattern(2.0, new int[]{0, 1, 2, 6, 7, 8});
            rules.addWinPattern(1.5, new int[]{0, 4, 8});
            rules.addWinPattern(1.3, new int[]{2, 4, 6});
            rules.addWinPattern(1.1, new int[]{0, 1, 2});
            rules.addWinPattern(1.0, new int[]{3, 4, 5});
            rules.addWinPattern(0.5, new int[]{3, 5});
            rules.addWinPattern(0.1, new int[]{4});
        }
        return rules;
    }
}