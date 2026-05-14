package gr.goldenbet.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

public class PlayerStats implements Serializable {
    private BigDecimal totalProfit;
    private double winRate;
    private BigDecimal profitPerAttempt;
    private BigDecimal profitPerWin;
    private BigDecimal lossPerLoss;
    private BigDecimal biggestWin;
    private String mostPlayedGame;
    private Map<String, BigDecimal> weeklyActivity;

    // Constructor
    public PlayerStats(BigDecimal totalProfit, double winRate, BigDecimal profitPerAttempt,
                       BigDecimal profitPerWin, BigDecimal lossPerLoss, BigDecimal biggestWin,
                       String mostPlayedGame,Map<String, BigDecimal> weeklyActivity) {
        this.totalProfit = totalProfit;
        this.winRate = winRate;
        this.profitPerAttempt = profitPerAttempt;
        this.profitPerWin = profitPerWin;
        this.lossPerLoss = lossPerLoss;
        this.biggestWin = biggestWin;
        this.mostPlayedGame = mostPlayedGame;
        this.weeklyActivity = weeklyActivity;
    }

    // Getters & Setters
    public BigDecimal getTotalProfit() { return totalProfit; }
    public void setTotalProfit(BigDecimal totalProfit) { this.totalProfit = totalProfit; }

    public double getWinRate() { return winRate; }
    public void setWinRate(double winRate) { this.winRate = winRate; }

    public BigDecimal getProfitPerAttempt() { return profitPerAttempt; }
    public void setProfitPerAttempt(BigDecimal profitPerAttempt) { this.profitPerAttempt = profitPerAttempt; }

    public BigDecimal getProfitPerWin() { return profitPerWin; }
    public void setProfitPerWin(BigDecimal profitPerWin) { this.profitPerWin = profitPerWin; }

    public BigDecimal getLossPerLoss() { return lossPerLoss; }
    public void setLossPerLoss(BigDecimal lossPerLoss) { this.lossPerLoss = lossPerLoss; }

    public BigDecimal getBiggestWin() { return biggestWin; }
    public void setBiggestWin(BigDecimal biggestWin) { this.biggestWin = biggestWin; }

    public String getMostPlayedGame() { return mostPlayedGame; }

    public Map<String, BigDecimal> getWeeklyActivity() { return weeklyActivity; }
    public void setWeeklyActivity(Map<String, BigDecimal> weeklyActivity) { this.weeklyActivity = weeklyActivity; }
}