package gr.goldenbet.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class Deck {
    private final String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9",
            "10", "j", "q", "k", "a"};
    private final String[] suits = {"hearts", "diamonds", "clubs", "spades"};
    private ArrayList<String> cards = new ArrayList<>();

    public Deck() {
    }

    public ArrayList<String> getCards() {
        if(cards.isEmpty()) {
            for (String suit : suits) {
                for (String rank : ranks) {
                    cards.add(suit + "_" + rank);
                }
            }
        }
        Collections.shuffle(cards);
        return cards;
    }

    public ArrayList<String> winpattern(double multiplier) {
        ArrayList<String> hand = new ArrayList<>();

        if (multiplier == 40.0) {
            hand.add("clubs_10");
            hand.add("clubs_j");
            hand.add("clubs_q");
            hand.add("clubs_k");
            hand.add("clubs_a");

        } else if (multiplier == 6.5) {
            Collections.shuffle(Arrays.asList(suits));
            hand.add(suits[0] + "_8");
            hand.add(suits[1] + "_8");
            hand.add(suits[2] + "_8");

            Collections.shuffle(Arrays.asList(suits));
            hand.add(suits[0] + "_k");
            hand.add(suits[1] + "_k");

        } else if (multiplier == 4.0) {

            Collections.shuffle(Arrays.asList(ranks));

            for (int i = 0; i < 5; i++) {
                hand.add("hearts_" + ranks[i]);
            }

        } else if (multiplier == 1.0) {

            Collections.shuffle(Arrays.asList(ranks));
            String pair1Rank = ranks[0];
            String pair2Rank = ranks[1];
            String kickerRank = ranks[2];

            Collections.shuffle(Arrays.asList(suits));
            hand.add(suits[0] + "_" + pair1Rank);
            hand.add(suits[1] + "_" + pair1Rank);

            Collections.shuffle(Arrays.asList(suits));
            hand.add(suits[0] + "_" + pair2Rank);
            hand.add(suits[1] + "_" + pair2Rank);

            hand.add(suits[0] + "_" + kickerRank);

        } else {

            for (int i = 0; i < 5; i++) {
               hand.add(getCards().remove(0));
            }
        }

        return hand;
    }
}



