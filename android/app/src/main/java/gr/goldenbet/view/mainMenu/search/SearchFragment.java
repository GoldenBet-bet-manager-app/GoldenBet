package gr.goldenbet.view.mainMenu.search;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import gr.goldenbet.R;
import gr.goldenbet.databinding.FragmentSearchBinding;
import gr.goldenbet.domain.Game;
import gr.goldenbet.domain.Player;
import gr.goldenbet.view.games.fruitgame.FruitGameFragment;
import gr.goldenbet.view.games.poker.PokerFragment;
import gr.goldenbet.view.games.wheelgame.WheelGameFragment;
import gr.goldenbet.view.mainMenu.MainMenuActivity;
import gr.goldenbet.view.mainMenu.home.deposit.DepositFragment;


public class SearchFragment extends Fragment implements SearchView, SearchAdapter.SearchListener {

    private FragmentSearchBinding binding;
    private SearchPresenter presenter;
    private SearchAdapter adapter;


    public SearchFragment(){
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        SearchViewModel viewModel = new ViewModelProvider(requireActivity()).get(SearchViewModel.class);
        presenter =  viewModel.getPresenter();
        presenter.setView(this);

        binding.searchDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        setupRecyclerView();
        setupClickListeners();
        setupLiveSearch();

        if (getActivity() instanceof MainMenuActivity) {
            Player currentPlayer = ((MainMenuActivity) getActivity()).getCurrentPlayer();
            if (currentPlayer != null) {
                binding.txtSearchTokenBalance.setText(currentPlayer.getBalance() + " FUN");
            }
        }
        presenter.loadGames();
    }

    private void setupRecyclerView(){

        adapter = new SearchAdapter(this);
        binding.rvGameResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvGameResults.setAdapter(adapter);
    }

    private void setupClickListeners(){

        binding.btnSearchFilterTrigger.setOnClickListener(v ->
                binding.searchDrawerLayout.openDrawer(GravityCompat.END));
        binding.includedFiltersDrawer.sliderStars.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            binding.includedFiltersDrawer.txtStarRange.setText(values.get(0) + " - " + values.get(1));
        });
        binding.includedFiltersDrawer.btnApplyFilters.setOnClickListener(v ->
                applyFiltersAndSearch());
        binding.includedFiltersDrawer.btnClearFilters.setOnClickListener(v ->
                clearFilters());
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            presenter.onSwipeToRefresh();
        });
        binding.btnSearchAddTokens.setOnClickListener(v -> navigateToDeposit());

    }

    private void setupLiveSearch() {

        binding.edtSearchGameName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                presenter.onSearchQueryChanged(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void applyFiltersAndSearch(){

        List<Float> starValues = binding.includedFiltersDrawer.sliderStars.getValues();
        float minStars = starValues.get(0);
        float maxStars = starValues.get(1);

        List<String> selectedRisks = new ArrayList<>();
        int riskCount = binding.includedFiltersDrawer.linearGroupRisk.getChildCount();
        for(int i = 0; i < riskCount; i++){
            View view = binding.includedFiltersDrawer.linearGroupRisk.getChildAt(i);
            if(view instanceof Chip){
                Chip chip = (Chip) view;
                if(chip.isChecked()){
                    String rawText = chip.getText().toString();
                    selectedRisks.add(rawText.split(" ")[0]);
                }
            }
        }

        List<String> selectedBets = new ArrayList<>();
        int betCount = binding.includedFiltersDrawer.linearGroupBet.getChildCount();
        for(int i = 0; i < betCount; i++){
            View view = binding.includedFiltersDrawer.linearGroupBet.getChildAt(i);
            if(view instanceof Chip){
                Chip chip = (Chip) view;
                if(chip.isChecked()){
                    String rawText = chip.getText().toString();
                    selectedBets.add(rawText.split(" ")[0]);
                }
            }
        }

        presenter.onApplyFilters(minStars, maxStars, selectedBets, selectedRisks);
        binding.searchDrawerLayout.closeDrawer(GravityCompat.END);
    }
    private void clearFilters(){

        binding.includedFiltersDrawer.sliderStars.setValues(0f, 5f);

        for (int i = 0; i < binding.includedFiltersDrawer.linearGroupRisk.getChildCount(); i++) {
            View view = binding.includedFiltersDrawer.linearGroupRisk.getChildAt(i);
            if (view instanceof Chip) {
                ((Chip) view).setChecked(false);
            }
        }

        for (int i = 0; i < binding.includedFiltersDrawer.linearGroupBet.getChildCount(); i++) {
            View view = binding.includedFiltersDrawer.linearGroupBet.getChildAt(i);
            if (view instanceof Chip) {
                ((Chip) view).setChecked(false);
            }
        }

        presenter.onResetFilters();
        binding.searchDrawerLayout.closeDrawer(GravityCompat.END);
    }

    private void navigateToDeposit(){

        DepositFragment depositFragment = new DepositFragment();

        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_container, depositFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onGameClicked(Game game){

        Fragment gameFragment = null;

        if (game.getGameCategory() != null) {
            switch (game.getGameCategory().toUpperCase()) {
                case "FRUIT":
                    gameFragment = FruitGameFragment.newInstance(game.getGameName());
                    break;
                case "POKER":
                    gameFragment = PokerFragment.newInstance(game.getGameName());
                    break;
                case "WHEEL":
                    gameFragment = WheelGameFragment.newInstance(game.getGameName());
                    break;
                default:
                    showMessage("Η κατηγορία αυτού του παιχνιδιού δεν υποστηρίζεται ακόμα.");
                    return;
            }
        } else {
            showMessage("Άγνωστη κατηγορία παιχνιδιού.");
            return;
        }

        if (gameFragment != null) {

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                            R.anim.slide_in_left, R.anim.slide_out_right)
                    .replace(R.id.fragment_container, gameFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void updateGameList(List<Game> gamesToShow){
        adapter.updateList(gamesToShow);
    }

    @Override
    public void showEmptyState(){
        binding.tvEmptyState.setVisibility(View.VISIBLE);
        binding.rvGameResults.setVisibility(View.GONE);
    }

    @Override
    public void hideEmptyState(){
        binding.tvEmptyState.setVisibility(View.GONE);
        binding.rvGameResults.setVisibility(View.VISIBLE);
    }

    /**
     *  Ενεργοποιεί το εικονίδιο φόρτωσης του SwipeRefreshLayout
     */
    @Override
    public void showLoading(){
        binding.swipeRefreshLayout.setRefreshing(true);
    }

    /**
     *  Σταματάει το εικονίδιο φόρτωσης του SwipeRefreshLayout
     */
    @Override
    public void hideLoading(){
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void clearSearchBar(){
        binding.edtSearchGameName.setText("");
    }

    @Override
    public void showMessage(String message){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onDestroyView(){

        super.onDestroyView();
        if (presenter != null) {
            presenter.clearView();
        }
        binding = null;
    }
}