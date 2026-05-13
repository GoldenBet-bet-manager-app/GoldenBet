package gr.goldenbet.view.mainMenu.home.stats;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import gr.goldenbet.domain.HistoryFilters;
import gr.goldenbet.domain.JsonClient;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.PlayerStats;
import gr.goldenbet.domain.Transaction;
import gr.goldenbet.utils.ConfigLoader;

public class StatsPresenter {
    private final StatsView view;
    private final String masterIp;
    private final int masterPort;

    public StatsPresenter(StatsView view) {
        this.view = view;
        this.masterIp = ConfigLoader.getIp("MASTER");
        this.masterPort = ConfigLoader.getPort("MASTER");
    }

    public void loadPlayerStats(String playerId, String playerUsername) {
        view.showLoading();

        new Thread(() -> {
            Gson gson = new Gson();


            ArrayList<String> types = new ArrayList<>();
            types.add("All");
            HistoryFilters filters = new HistoryFilters(playerId ,playerUsername, types, "");
            MasterMessage request = new MasterMessage(MasterMessageType.GET_HISTORY, filters);


            JsonClient client = new JsonClient(masterIp, masterPort, gson.toJson(request));
            client.start();

            try {

                client.join();

                String response = client.getJsonResponse();
                if (response != null) {
                    ArrayList<Transaction> transactions = gson.fromJson(response,
                            new TypeToken<ArrayList<Transaction>>(){}.getType());

                    processAndDeliver(transactions);
                } else {
                    view.showError("No response from server");
                }

            } catch (Exception e) {
                view.showError("Communication error");
            }
        }).start();
    }

    private void processAndDeliver(ArrayList<Transaction> txs) {
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal winSum = BigDecimal.ZERO;
        BigDecimal lossSum = BigDecimal.ZERO;
        BigDecimal maxWin = BigDecimal.ZERO;
        int bets = 0;
        int wins = 0;

        HashMap<String, Integer> gameCounts = new HashMap<>();
        Map<String, BigDecimal> activity = new HashMap<>();
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for(String d : days) activity.put(d, BigDecimal.ZERO);

        if (txs == null || txs.isEmpty()) {
            Log.d("Stats Presenter", "Transactions list is empty or null");
        }

        for (Transaction tx : txs) {
            if (tx == null || tx.getType() == null) continue;
            if (tx.getType().equals("DEPOSIT")) continue;

            BigDecimal amount = (tx.getAmount() != null) ? tx.getAmount() : BigDecimal.ZERO;
            totalProfit = totalProfit.add(amount);

            if (tx.getType().equals("GAME_BET")) {
                bets++;
                lossSum = lossSum.add(amount.abs());

                // Most Played Game Logic - Με null safety για το description
                String desc = (tx.getDescription() != null) ? tx.getDescription() : "";
                String gameName = desc.replace("Bet on ", "").trim();
                if (!gameName.isEmpty()) {
                    gameCounts.put(gameName, gameCounts.getOrDefault(gameName, 0) + 1);
                }
            }
            else if (tx.getType().equals("GAME_RESULT")) {
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    wins++;
                    winSum = winSum.add(amount);
                    if (amount.compareTo(maxWin) > 0) maxWin = amount;
                }
            }

            // Weekly Chart Logic
            try {
                String txId = tx.getTransactionId();
                if (txId != null) {
                    String numericPart = txId.replaceAll("[^0-9]", "");

                    if (!numericPart.isEmpty()) {
                        long ts = Long.parseLong(numericPart);
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(ts);

                        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                        String key = days[dayOfWeek - 1];

                        BigDecimal currentDayVal = activity.get(key);
                        activity.put(key, currentDayVal.add(amount));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Calculation of KPIs (ppa, ppw, lpl, winRate)
        double winRate = bets > 0 ? ((double) wins / bets) * 100 : 0;
        BigDecimal ppa = bets > 0 ? totalProfit.divide(new BigDecimal(bets), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal ppw = wins > 0 ? winSum.divide(new BigDecimal(wins), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal lpl = (bets - wins) > 0 ? lossSum.divide(new BigDecimal(bets - wins), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // Find the Most Played Game
        String mostPlayed = "None";
        if (!gameCounts.isEmpty()) {
            mostPlayed = gameCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey).orElse("None");
        }

        // Creation of PlayerStats object with the updated data
        PlayerStats stats = new PlayerStats(totalProfit, winRate, ppa, ppw, lpl, maxWin, mostPlayed, activity);



        view.showStats(stats);
    }
}