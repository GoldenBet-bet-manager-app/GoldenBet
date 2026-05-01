package gr.goldenbet.view.mainMenu.home.history;

public class HistoryEntry {
    public enum Type { BET, WIN, DEPOSIT }

    private Type type;
    private String title;
    private String dateTime;
    private double amount;

    public HistoryEntry(Type type, String title, String dateTime, double amount) {
        this.type = type;
        this.title = title;
        this.dateTime = dateTime;
        this.amount = amount;
    }

    public Type getType() { return type; }
    public String getTitle() { return title; }
    public String getDateTime() { return dateTime; }
    public double getAmount() { return amount; }
}