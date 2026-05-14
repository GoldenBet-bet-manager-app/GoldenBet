package gr.goldenbet.domain;

import java.util.ArrayList;
import java.util.Collections;

public class PokerGameRules {
    private final String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "j", "q", "k", "a"};
    private final String[] suits = {"hearts", "diamonds", "clubs", "spades"};
    private ArrayList<String> cards = new ArrayList<>();
    private String handName;

    public PokerGameRules() {
    }

    public ArrayList<String> getCards() {
        if (cards.isEmpty()) {
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

        // --- JACKPOTS (10.0, 20.0, 40.0) ---
        if (multiplier == 40.0 || multiplier == 20.0 || multiplier == 10.0) {
            handName = "Royal Flush 👑";
            hand.add("spades_10");
            hand.add("spades_j");
            hand.add("spades_q");
            hand.add("spades_k");
            hand.add("spades_a");

            // --- HIGH RISK TOP WIN (6.5) ---
        } else if (multiplier == 6.5) {
            handName = "Full House";
            hand.add("hearts_8");
            hand.add("diamonds_8");
            hand.add("clubs_8");
            hand.add("hearts_k");
            hand.add("diamonds_k");

            // --- MEDIUM RISK TOP WIN (3.5) ---
        } else if (multiplier == 3.5) {
            handName = "Four of a Kind";
            hand.add("hearts_a");
            hand.add("diamonds_a");
            hand.add("clubs_a");
            hand.add("spades_a");
            hand.add("hearts_q");

            // --- LOW / MEDIUM WINS (2.5) ---
        } else if (multiplier == 2.5) {
            handName = "Full House";
            hand.add("hearts_q");
            hand.add("diamonds_q");
            hand.add("spades_q");
            hand.add("clubs_9");
            hand.add("spades_9");

            // --- FLUSH WINS (2.0 , 4.0) ---
        } else if (multiplier == 2.0 || multiplier == 4.0) {
            handName = "Flush";
            hand.add("hearts_2");
            hand.add("hearts_5");
            hand.add("hearts_7");
            hand.add("hearts_j");
            hand.add("hearts_k");

            // --- STRAIGHT WINS (1.3 , 1.5) ---
        } else if (multiplier == 1.5 || multiplier == 1.3) {
            handName = "Straight";
            hand.add("hearts_5");
            hand.add("diamonds_6");
            hand.add("clubs_7");
            hand.add("spades_8");
            hand.add("hearts_9");

            // --- THREE OF A KIND (1.1) ---
        } else if (multiplier == 1.1) {
            handName = "Three of a Kind";
            hand.add("clubs_7");
            hand.add("diamonds_7");
            hand.add("spades_7");
            hand.add("hearts_j");
            hand.add("diamonds_2");

            // --- TWO PAIR (1.0) ---
        } else if (multiplier == 1.0) {
            handName = "Two Pair";
            hand.add("hearts_j");
            hand.add("diamonds_j");
            hand.add("clubs_5");
            hand.add("spades_5");
            hand.add("hearts_a");

            // --- ONE PAIR (0.5) ---
        } else if (multiplier == 0.5) {
            handName = "One Pair";
            hand.add("hearts_10");
            hand.add("spades_10");
            hand.add("diamonds_2");
            hand.add("clubs_4");
            hand.add("hearts_k");

            // --- HIGH CARD / MINOR WIN (0.1) ---
        } else if (multiplier == 0.1) {
            handName = "High Card";
            hand.add("hearts_a");
            hand.add("diamonds_j");
            hand.add("clubs_8");
            hand.add("spades_5");
            hand.add("hearts_2");

            // --- LOSS (0.0) ---
        } else {
            handName = "";
            for (int i = 0; i < 5; i++) {
                hand.add(getCards().remove(0));
            }
        }

        return hand;
    }

    public String getHandName() {
        return handName;
    }
}