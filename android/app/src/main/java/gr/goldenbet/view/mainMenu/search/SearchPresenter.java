package gr.goldenbet.view.mainMenu.search;



import android.app.Activity;



import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;



import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import java.util.List;

import java.util.Objects;



import gr.goldenbet.domain.Game;

import gr.goldenbet.domain.MasterMessage;

import gr.goldenbet.domain.MasterMessageType;

import gr.goldenbet.domain.SearchFilters;

import gr.goldenbet.utils.NetworkManager;



public class SearchPresenter{



    private SearchView view;

    private final String MASTER_IP = "192.168.1.79";

    private final int MASTER_PORT = 8080;



    private boolean hasLoadedData = false;



    private List<Game> gameList;

    private String currentQuery = "";

    private float currentMinStars = 0f;

    private float currentMaxStars = 5f;

    private List<String> currentBetLimits = new ArrayList<>();

    private List<String> currentRiskLevels = new ArrayList<>();





    public SearchPresenter(){

        this.gameList = new ArrayList<>();

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



        fetchGamesFromServer();

    }





    private void updateViewWithCurrentList(){



        if(view != null){



            List<Game> gamesToShow = new ArrayList<>();



            if(currentQuery.trim().isEmpty() || Objects.equals(currentQuery, "")){

                gamesToShow = gameList;

            }else{

                for (Game game : gameList) {

                    if (game.getGameName().toLowerCase().contains(currentQuery)) {

                        gamesToShow.add(game);

                    }

                }

            }



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



        String riskString = String.join(",", currentRiskLevels);

        String betString = String.join(",", currentBetLimits);



        SearchFilters filters = new SearchFilters(currentMinStars, currentMaxStars, betString, riskString);



        MasterMessage searchMsg = new MasterMessage(MasterMessageType.SEARCH, filters);

        String jsonToSend = new Gson().toJson(searchMsg);



        NetworkManager.sendRequest(MASTER_IP, MASTER_PORT, jsonToSend)

                .thenAccept(response -> {

                    try {

                        if (response != null){



                            ArrayList<Game> fetchedGames = new Gson().fromJson(response, new TypeToken<ArrayList<Game>>(){}.getType());



                            if (view != null) {

                                ((Fragment) view).requireActivity().runOnUiThread(() -> {

                                    gameList.clear();

                                    if (fetchedGames != null) {

                                        gameList.addAll(fetchedGames);

                                    }

                                    hasLoadedData = true;

                                    updateViewWithCurrentList();

                                    view.hideLoading();

                                });

                            }

                        } else {

                            if (view != null) {

                                ((Activity) view).runOnUiThread(() -> {

                                    view.hideLoading();

                                    view.showMessage("Σφάλμα επικοινωνίας με τον Server.");

                                });

                            }

                        }

                    } catch (Exception e) {

                        e.printStackTrace();

                        if (view != null) {

                            ((Fragment) view).requireActivity().runOnUiThread(() -> {

                                view.hideLoading();

                                view.showMessage("Σφάλμα ανάγνωσης δεδομένων από τον Server!");

                            });

                        }

                    }

                }).exceptionally(e -> {

                    // Εδώ πιάνουμε σφάλματα που έγιναν ΠΡΙΝ καν φτάσει η απάντηση (π.χ. έπεσε το ίντερνετ)

                    e.printStackTrace();

                    if (view != null) {

                        ((Fragment) view).requireActivity().runOnUiThread(() -> {

                            view.hideLoading();

                            view.showMessage("Αδυναμία σύνδεσης στον Server.");

                        });

                    }

                    return null;

                });

    }



    public void setView(SearchView view){

        this.view = view;

    }



    public void clearView() {

        this.view = null;

    }





}