package gr.goldenbet.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

public class PlayerStats implements Serializable {
    public BigDecimal totalProfit;
    public double winRate;
    public BigDecimal profitPerAttempt;
    public BigDecimal avgLoss;
    public BigDecimal avgWin;
    public String mostTimeGame;
    public String mostFunGame;
    public BigDecimal biggestWin;
    public Map<String, BigDecimal> weeklyActivity;
}