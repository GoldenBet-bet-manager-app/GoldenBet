package gr.goldenbet.view.mainMenu.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import gr.goldenbet.databinding.FragmentHomeBinding;
import gr.goldenbet.view.mainMenu.MainMenuActivity;

public class HomeFragment extends Fragment implements HomeView {

    // Binding is used to have access to the IDs of the XML
    private FragmentHomeBinding binding;
    private HomePresenter presenter;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Αρχικοποίηση του Presenter
        presenter = new HomePresenter(this);

        setupClickListeners();

        presenter.initData();
    }

    private void setupClickListeners() {
        binding.cardDeposit.setOnClickListener(v -> presenter.onDepositClicked());
        binding.cardHistory.setOnClickListener(v -> presenter.onHistoryClicked());
        binding.cardStats.setOnClickListener(v -> presenter.onStatsClicked());
        binding.cardRatings.setOnClickListener(v -> presenter.onRatingsClicked());
    }


    @Override
    public void setPlayerName(String name) {
        binding.txtHomePlayerName.setText(name);
    }

    @Override
    public void setBalance(String balance) {
        binding.txtHomeBalance.setText(balance);
    }

    @Override
    public void navigateToDeposit() {
        // Εδώ θα καλέσεις το navigation προς την κατάθεση
        Toast.makeText(getContext(), "Μεταφορά στην Κατάθεση...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToHistory() {
        Toast.makeText(getContext(), "Άνοιγμα Ιστορικού...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToStats() {
        Toast.makeText(getContext(), "Προβολή Στατιστικών...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToRatings() {
        Toast.makeText(getContext(), "Αξιολογήσεις...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // binding is nullified not to have memory leaks
        binding = null;
    }
}