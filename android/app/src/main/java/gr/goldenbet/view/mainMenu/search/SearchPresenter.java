package gr.goldenbet.view.mainMenu.search;

import static java.util.Collections.sort;

import android.app.Activity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gr.goldenbet.domain.Game;
import gr.goldenbet.domain.JsonClient;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.SearchFilters;
import gr.goldenbet.utils.ConfigLoader;
import gr.goldenbet.utils.NetworkManager;

public class SearchPresenter{

    private SearchView view;
    private final String MASTER_IP;
    private final int MASTER_PORT;

    private boolean hasLoadedData = false;
    private int currentRequestId = 0;

    private final List<Game> gameList;
    private String currentQuery = "";
    private float currentMinStars = 0f;
    private float currentMaxStars = 5f;
    private List<String> currentBetLimits = new ArrayList<>();
    private List<String> currentRiskLevels = new ArrayList<>();


    public SearchPresenter(){

        this.gameList = new ArrayList<>();
        MASTER_IP = ConfigLoader.getIp("MASTER");
        MASTER_PORT = ConfigLoader.getPort("MASTER");
    }


    public void loadGames(){

        if(hasLoadedData){
            updateViewWithCurrentList();
        }else{
            fetchGamesFromServer();
        }
    }

    public void onSwipeToRefresh(){

        fetchGamesFromServer();
    }

    public void onSearchQueryChanged(String query) {
        this.currentQuery = query.toLowerCase().trim();
        updateViewWithCurrentList();
    }

    public void onApplyFilters(float minStars, float maxStars, List<String> betLimits, List<String> risks){

        this.currentMinStars = minStars;
        this.currentMaxStars = maxStars;
        this.currentBetLimits = betLimits;
        this.currentRiskLevels = risks;

        fetchGamesFromServer();
    }

    public void onResetFilters(){

        this.currentQuery = "";
        this.currentMinStars = 0f;
        this.currentMaxStars = 5f;
        this.currentBetLimits.clear();
        this.currentRiskLevels.clear();

        view.clearSearchBar();
        fetchGamesFromServer();
    }


    private void updateViewWithCurrentList(){

        if(view != null){

            List<Game> gamesToShow = new ArrayList<>();

            if(currentQuery.trim().isEmpty() || Objects.equals(currentQuery, "")){
                gamesToShow.addAll(gameList);
            }else{
                for (Game game : gameList) {
                    if (game.getGameName().toLowerCase().contains(currentQuery)) {
                        gamesToShow.add(game);
                    }
                }
            }
            //ταξινόμηση κατα βαθμολογία παιχνιδιού
            sort(gamesToShow, (g1, g2) ->
                    Float.compare(g2.getStars(), g1.getStars())
            );

            view.updateGameList(gamesToShow);

            if (gamesToShow.isEmpty()) {
                view.showEmptyState();
                view.hideLoading();
            } else {
                view.hideEmptyState();
            }
        }

    }

    private void fetchGamesFromServer(){

        if (view != null) {
            view.showLoading();
        }

        currentRequestId++;
        int myTicket = currentRequestId;

        String riskString = String.join(",", currentRiskLevels);
        String betString = String.join(",", currentBetLimits);

        SearchFilters filters = new SearchFilters(currentMinStars, currentMaxStars, betString, riskString);

        MasterMessage searchMsg = new MasterMessage(MasterMessageType.SEARCH, filters);
        String jsonToSend = new Gson().toJson(searchMsg);

        new Thread(()->{
            try {

                JsonClient client = new JsonClient(MASTER_IP, MASTER_PORT, jsonToSend);
                client.start();
                client.join();

                String response = client.getJsonResponse();

                if (myTicket != currentRequestId) return;
                if (response != null){
                    try {
                        ArrayList<Game> fetchedGames = new Gson().fromJson(response, new TypeToken<ArrayList<Game>>() {
                        }.getType());

                        if (view != null) {
                            ((Fragment) view).requireActivity().runOnUiThread(() -> {
                                if (view == null) return;
                                gameList.clear();
                                if (fetchedGames != null) {
                                    gameList.addAll(fetchedGames);
                                }
                                hasLoadedData = true;
                                updateViewWithCurrentList();
                                view.hideLoading();
                            });
                        }

                    }catch (Exception e){
                        if (view != null) {
                            ((Fragment) view).requireActivity().runOnUiThread(() -> {
                                if (view == null) return;
                                view.hideLoading();
                                view.showMessage("Προέκυψε σφάλμα. Ξανακάντε αναζήτηση παιχνδιών");
                            });
                        }
                    }
                }

                else {
                    if (view != null) {
                        ((Fragment) view).requireActivity().runOnUiThread(() -> {
                            if (view == null) return;
                            view.hideLoading();
                            view.showMessage("Σφάλμα επικοινωνίας με τον Server.");
                        });
                    }
                }

            }catch (InterruptedException e) {
                e.printStackTrace();
                view.showMessage("Connection interrupted");
            }
        }).start();


       // NetworkManager.sendRequest(MASTER_IP, MASTER_PORT, jsonToSend)
               // .thenAccept(response -> {


              //});
    }

    public void setView(SearchView view){
        this.view = view;
    }

    public void clearView() {
        this.view = null;
    }


}
