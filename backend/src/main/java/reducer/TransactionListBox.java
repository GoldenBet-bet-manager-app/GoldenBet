package reducer;

import Models.Transaction;
import java.util.ArrayList;

public class TransactionListBox implements AggregationBox {
    private ArrayList<Transaction> allTransactions = new ArrayList<>();
    private int receivedResponses = 1;

    public TransactionListBox(ArrayList<Transaction> initialList) {
        this.allTransactions.addAll(initialList);
    }

    @Override
    public void addGamesFromWorker(Object payload) {
        if (payload instanceof ArrayList) {
            allTransactions.addAll((ArrayList<Transaction>) payload);
        }
    }

    @Override
    public Object getGamesFromWorkers() { return allTransactions; }

    @Override
    public int getReceivedResponses() { return receivedResponses; }

    @Override
    public void addReceivedResponses() { this.receivedResponses++; }
}