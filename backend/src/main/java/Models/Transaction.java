package Models;

import java.io.Serializable;
import java.math.BigDecimal;

public class Transaction implements Serializable {
    private String transactionId;
    private String playerId;
    private String username;
    private String type;
    private BigDecimal amount;
    private String timestamp; // "yyyy-MM-dd HH:mm:ss"
    private String description; // Transaction's description

    public Transaction(String playerId,String username, String type, BigDecimal amount, String description) {
        this.transactionId = "TX-" + System.currentTimeMillis();
        this.playerId = playerId;
        this.username = username;
        this.type = type;
        this.amount = amount;
        this.description = description;

        // Simple Date Format
        this.timestamp = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                .format(new java.util.Date());
    }

    // Getters
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getTimestamp() { return timestamp; }
    public String getDescription() { return description; }
    public String getTransactionId() {return transactionId;}
    public String getPlayerId() {return playerId;}
    public String getUsername(){return username;}
}