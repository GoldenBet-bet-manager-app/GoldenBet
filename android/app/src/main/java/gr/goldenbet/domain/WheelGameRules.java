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
            return new WheelGameRules(R.drawable.pharaohs_dynasty_logo,
                    new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.5, 1.0, 1.5, 2.5, 3.5, 20.0});

        } else if (gameName.equals("Dragon's Lair")) {
            return new WheelGameRules(R.drawable.dragons_lair_logo,
                    new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 2.0, 6.5, 40.0});

        } else if (gameName.equals("Pirate's Bounty")) {
            return new WheelGameRules(R.drawable.pirates_bounty_logo,
                    new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.5, 1.0, 1.5, 2.5, 3.5, 20.0});
        }
        return null;
    }

    public int determineSliceIndex(double multiplier) {
        ArrayList<Integer> matchingIndices = new ArrayList<>();

        // Ψάχνει ποιο/ποια slices (από το 0 έως το 10) έχουν αυτόν τον πολλαπλασιαστή
        for (int i = 0; i < slices.length; i++) {
            if (slices[i] == multiplier) {
                matchingIndices.add(i);
            }
            // Αν βρήκε παραπάνω από ένα slices (π.χ. τα πολλά 0.0),
            // διαλέγει ένα στην τύχη για να είναι ρεαλιστικό το γύρισμα.
            // Αν βρήκε μόνο ένα (όπως το Jackpot στη θέση 10), θα επιστρέψει κατευθείαν αυτό!
            if (!matchingIndices.isEmpty()) {
                Random random = new Random();
                int randomIndex = random.nextInt(matchingIndices.size());
                return matchingIndices.get(randomIndex);
            }
        }
        return 0; // Ασφάλεια σε περίπτωση λάθους
    }

    public int getLogoResId() {
        return logoResId;
    }

    public double[] getSlices() {
        return slices;
    }
}


