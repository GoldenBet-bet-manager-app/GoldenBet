package gr.goldenbet.view.mainMenu.home.history;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import gr.goldenbet.R;
import gr.goldenbet.domain.HistoryEntry;
import gr.goldenbet.domain.HistoryFilters;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.Transaction;
import gr.goldenbet.domain.JsonClient;
import gr.goldenbet.utils.ConfigLoader;

public class HistoryPresenter {
    private HistoryView view;
    private String username;
    private Gson gson = new Gson();

    private final String MASTER_IP;
    private final int MASTER_PORT;

    public HistoryPresenter(HistoryView view, String username) {
        this.view = view;
        this.username = username;

        MASTER_IP = ConfigLoader.getIp("MASTER");
        MASTER_PORT = ConfigLoader.getPort("MASTER");
    }

    public void start() {
        loadHistory("All", "");
    }

    public void filterData(String query, String filterType) {
        loadHistory(filterType, query);
    }

    private void loadHistory(String filterType, String query) {
        new Thread(() -> {
            ArrayList<String> types = new ArrayList<>();
            types.add(filterType);

            HistoryFilters filters = new HistoryFilters(username, types, query);
            MasterMessage request = new MasterMessage(MasterMessageType.GET_HISTORY, filters);
            String requestJson = gson.toJson(request);

            JsonClient client = new JsonClient(MASTER_IP, MASTER_PORT, requestJson);
            client.start();

            try {
                client.join();
                String responseJson = client.getJsonResponse();

                if (responseJson != null) {
                    Type listType = new TypeToken<ArrayList<Transaction>>(){}.getType();
                    ArrayList<Transaction> transactions = gson.fromJson(responseJson, listType);

                    List<HistoryEntry> uiEntries = new ArrayList<>();
                    for (Transaction tx : transactions) {
                        uiEntries.add(mapToUI(tx));
                    }

                    if (view instanceof Fragment) {
                        Activity activity = ((Fragment) view).getActivity();
                        if (activity != null) {
                            activity.runOnUiThread(() -> view.showHistory(uiEntries));
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private HistoryEntry mapToUI(Transaction tx) {
        HistoryEntry.Type uiType;
        if (tx.getType().equals("GAME_RESULT")) uiType = HistoryEntry.Type.WIN;
        else if (tx.getType().equals("GAME_BET")) uiType = HistoryEntry.Type.BET;
        else uiType = HistoryEntry.Type.DEPOSIT;

        return new HistoryEntry(uiType, tx.getDescription(), tx.getTimestamp(), tx.getAmount());
    }
}