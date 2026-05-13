package gr.goldenbet.domain;

import java.util.ArrayList;
import java.util.Random;

import gr.goldenbet.R;

public class WheelGameRules {

    private int logoResId;
    private double[] slices;

    public WheelGameRules(int logoResId, double[] slices) {
        this.logoResId = logoResId;
        this.slices = slices;
    }

    public static WheelGameRules getWheelGraphics(String gameName) {
        if (gameName.equals("Pharaoh's Dynasty")) {

            return new WheelGameRules(R.drawable.wheel_medium,
                    new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.5, 1.0, 1.5, 2.5, 3.5, 20.0});

        } else if (gameName.equals("Dragon's Lair")) {
            return new WheelGameRules(R.drawable.wheel_high,
                    new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 2.0, 6.5, 40.0, 0.0});

        } else if (gameName.equals("Pirate's Bounty")) {
            return new WheelGameRules(R.drawable.wheel_medium,
                    new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.5, 1.0, 1.5, 2.5, 3.5, 20.0});
        }
        return null;
    }


    public int determineSliceIndex(double multiplier) {
        ArrayList<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < slices.length; i++) {
            if (slices[i] == multiplier) {
                matchingIndices.add(i);
            }
        }

        if (!matchingIndices.isEmpty()) {
            Random random = new Random();
            int randomIndex = random.nextInt(matchingIndices.size());
            return matchingIndices.get(randomIndex);
        }

        return 0;
    }

    public int getLogoResId() {
        return logoResId;
    }

    public double[] getSlices() {
        return slices;
    }
}


