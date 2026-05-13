package gr.goldenbet.view.mainMenu.home.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.ChipGroup;
import gr.goldenbet.R;
import gr.goldenbet.domain.HistoryEntry;
import gr.goldenbet.view.mainMenu.MainMenuActivity;

import java.util.List;

public class HistoryFragment extends Fragment implements HistoryView {

    private HistoryPresenter presenter;
    private RecyclerView recyclerView;
    private String currentFilter = "All";
    private String currentQuery = "";

    public HistoryFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String playerId = "";
        String playerUsername = "";
        if (getActivity() instanceof MainMenuActivity) {
            playerId = ((MainMenuActivity) getActivity()).getCurrentPlayer().getPlayerId();
            playerUsername = ((MainMenuActivity) getActivity()).getCurrentPlayer().getUsername();
        }

        presenter = new HistoryPresenter(this, playerId, playerUsername);

        recyclerView = view.findViewById(R.id.rvHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Back Button Logic
        view.findViewById(R.id.btnBack).setOnClickListener(v -> navigateBack());

        // Search Logic
        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                presenter.filterData(currentQuery, currentFilter);
                return true;
            }
        });

        // Chips Logic
        ChipGroup chipGroup = view.findViewById(R.id.filterChipGroup);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int id = checkedIds.get(0);
                if (id == R.id.chipAll) currentFilter = "All";
                else if (id == R.id.chipWins) currentFilter = "Wins";
                else if (id == R.id.chipBets) currentFilter = "Bets";
                else if (id == R.id.chipDeposits) currentFilter = "Deposits";

                presenter.filterData(currentQuery, currentFilter);
            }
        });

        presenter.start();
    }

    @Override
    public void showHistory(List<HistoryEntry> historyList) {
        recyclerView.setAdapter(new HistoryAdapter(historyList));
    }

    @Override
    public void navigateBack() {
        if (getFragmentManager() != null) {
            getFragmentManager().popBackStack();
        }
    }
}