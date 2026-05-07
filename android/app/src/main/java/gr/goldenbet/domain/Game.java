package gr.goldenbet.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public class Game implements Serializable {

    private String gameName;
    private String providerName;
    private float stars;
    private int votes;
    private String gameLogoPath;
    private BigDecimal minBet;
    private BigDecimal maxBet;
    private String riskLevel;
    private String hashKey;
    private String betCategory;
    private BigDecimal jackpot;
    private BigDecimal[] multipliers;
    private boolean active = true;
    private boolean backUp = false;
    private String gameCategory;
    private String base64Image;


    public Game(String gameName, String providerName, float stars, int votes,
                String gameLogoPath, BigDecimal minBet, BigDecimal maxBet,
                String riskLevel, String hashKey) {
        super();
        this.gameName = gameName;
        this.providerName = providerName;
        this.stars = stars;
        this.votes = votes;
        this.gameLogoPath = gameLogoPath;
        this.minBet = minBet;
        this.maxBet = maxBet;
        this.riskLevel = riskLevel;
        this.hashKey = hashKey;

        calculateBetCategory();
        setJackpotAndMultipliers();
    }


    public void calculateBetCategory() {
        // If min bet between 0.1 and 1
        if (this.minBet.compareTo(new BigDecimal("1.0")) < 0) {
            this.betCategory = "$";
            // If min bet between 1 and 5
        } else if (this.minBet.compareTo(new BigDecimal("5.0")) < 0) {
            this.betCategory = "$$";
            // If min bet greater than 5
        } else {
            this.betCategory = "$$$";
        }
    }


    public void setJackpotAndMultipliers() {
        if (this.riskLevel.equalsIgnoreCase("low")) {
            setJackpot(new BigDecimal(10));
            this.multipliers = new BigDecimal[]{
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    new BigDecimal("0.1"), new BigDecimal("0.5"), BigDecimal.ONE,
                    new BigDecimal("1.1"), new BigDecimal("1.3"), new BigDecimal("2.0"), new BigDecimal("2.5")
            };
        }
        if (this.riskLevel.equalsIgnoreCase("medium")) {
            setJackpot(new BigDecimal(20));
            this.multipliers = new BigDecimal[]{
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    new BigDecimal("0.5"), BigDecimal.ONE, new BigDecimal("1.5"),
                    new BigDecimal("2.5"), new BigDecimal("3.5")
            };
        }
        if (this.riskLevel.equalsIgnoreCase("high")) {
            setJackpot(new BigDecimal(40));
            this.multipliers = new BigDecimal[]{
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE,
                    new BigDecimal("2.0"), new BigDecimal("6.5")
            };
        }
    }


    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public float getStars() {
        return stars;
    }

    public void setStars(float stars) {
        this.stars = stars;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public String getGameLogoPath() {
        return gameLogoPath;
    }

    public void setGameLogoPath(String gameLogoPath) {
        this.gameLogoPath = gameLogoPath;
    }

    public BigDecimal getMinBet() {
        return minBet;
    }

    public void setMinBet(BigDecimal minBet) {
        this.minBet = minBet;
        calculateBetCategory();
    }

    public BigDecimal getMaxBet() {
        return maxBet;
    }

    public void setMaxBet(BigDecimal maxBet) {
        this.maxBet = maxBet;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
        setJackpotAndMultipliers();
    }

    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    public String getBetCategory() {
        return betCategory;
    }

    public void setBetCategory(String betCategory) {
        this.betCategory = betCategory;
    }

    public BigDecimal getJackpot() {
        return jackpot;
    }

    public void setJackpot(BigDecimal jackpot) {
        this.jackpot = jackpot;
    }

    public BigDecimal[] getMultipliers() {
        return multipliers;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isBackUp() {
        return backUp;
    }

    public void setBackUp(boolean backUp) {
        this.backUp = backUp;
    }

    public String getGameCategory ( ) {
        return gameCategory;
    }

    public void setGameCategory (String gameCategory) {
        this.gameCategory = gameCategory;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public void setBase64Image(String base64Image) {
        this.base64Image = base64Image;
    }

    @Override
    public String toString() {
        return "Game {" +
                "\n  Name:         '" + gameName + '\'' +
                "\n  Provider:     '" + providerName + '\'' +
                "\n  Rating:       " + stars + " stars (" + votes + " votes)" +
                "\n  Risk:         " + riskLevel.toUpperCase() +
                "\n  Bets:         " + minBet + "(min) - " + maxBet + "(max) [" + betCategory + "]" +
                "\n  Jackpot:      " + jackpot +
                "\n  Multipliers:  " + java.util.Arrays.toString(multipliers) +
                "\n  HashKey:      '" + hashKey + '\'' +
                "\n}";
    }
}

