package gr.goldenbet.view.mainMenu.home;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import gr.goldenbet.R;
import gr.goldenbet.databinding.FragmentHomeBinding;
import gr.goldenbet.domain.Player;
import gr.goldenbet.view.login.LoginActivity;
import gr.goldenbet.view.mainMenu.MainMenuActivity;
import gr.goldenbet.view.mainMenu.home.history.HistoryFragment;
import gr.goldenbet.view.mainMenu.home.rate.RateFragment;
import gr.goldenbet.view.mainMenu.home.deposit.DepositFragment;
import gr.goldenbet.view.mainMenu.home.stats.StatsFragment;
import gr.goldenbet.view.settings.settingsMenu.SettingsMenuActivity;

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
        presenter = new HomePresenter(this);

        setupClickListeners();

        // Get player from ViewModel of MainMenuActivity
        if (getActivity() instanceof MainMenuActivity) {
            Player player = ((MainMenuActivity) getActivity()).getCurrentPlayer();
            presenter.initData(player);
        }

        // Leader board red dot animation
        View liveDot = view.findViewById(R.id.liveDot);
        AlphaAnimation pulse = new AlphaAnimation(1.0f, 0.2f);
        pulse.setDuration(800);
        pulse.setRepeatCount(Animation.INFINITE);
        pulse.setRepeatMode(Animation.REVERSE);
        liveDot.startAnimation(pulse);
    }

    private void setupClickListeners() {
        binding.homeCardDeposit.setOnClickListener(v -> presenter.onDepositClicked());
        binding.homeCardHistory.setOnClickListener(v -> presenter.onHistoryClicked());
        binding.homeCardStats.setOnClickListener(v -> presenter.onStatsClicked());
        binding.homeCardRatings.setOnClickListener(v -> presenter.onRatingsClicked());
        binding.homeBtnLogout.setOnClickListener( v -> presenter.onLogoutClicked());
        binding.homeBtnSettings.setOnClickListener(v -> presenter.onSettingsClicked());
        binding.homeCardLeaderboardPreview.setOnClickListener(v -> presenter.onLeaderboardClicked());
    }


    @Override
    public void updatePlayerInfo(String name, String balance) {
        FragmentActivity activity = getActivity();

        // Check if binding is available
        if (binding != null) {
            binding.txtHomePlayerName.setText(name);
            binding.txtHomeBalance.setText(balance);
        }

    }

    @Override
    public void showLeaderboard(List<Map.Entry<String, BigDecimal>> data) {
        LeaderboardBottomSheet leaderboard = new LeaderboardBottomSheet(presenter,data);

        leaderboard.show(getChildFragmentManager(), "LeaderboardSheet");
    }

    @Override
    public void onLeaderboardUpdated(List<Map.Entry<String, BigDecimal>> sortedData) {
        LeaderboardBottomSheet fragment = (LeaderboardBottomSheet) getChildFragmentManager().findFragmentByTag("LeaderboardSheet");

        if (fragment != null) {
            fragment.onLeaderboardUpdated(sortedData);
        }


        getActivity().runOnUiThread(() -> {
            updateLeaderboardPreview(sortedData.subList(0, Math.min(5, sortedData.size())));
        });
    }

    @Override
    public void updateLeaderboardPreview(List<Map.Entry<String, BigDecimal>> topFive) {
        // Clean layout from previous updates not to double up
        binding.layoutTopFive.removeAllViews();

        // Display top 5 players profits
        int rank = 1;
        for (Map.Entry<String, BigDecimal> entry : topFive) {

            View row = getLayoutInflater().inflate(R.layout.item_leaderboard, binding.layoutTopFive, false);

            // Βρίσκουμε τα Views μέσα στη γραμμή (χωρίς binding εδώ για απλότητα)
            TextView txtRank = row.findViewById(R.id.txtRowRank);
            TextView txtUsername = row.findViewById(R.id.txtRowUsername);
            TextView txtProfit = row.findViewById(R.id.txtRowProfit);
            TextView txtYou = row.findViewById(R.id.txtYouTag);

            // Ανάθεση δεδομένων
            txtRank.setText("#" + rank);
            txtUsername.setText(entry.getKey());
            txtProfit.setText(String.format("%.2f €", entry.getValue()));

            // Έλεγχος αν είναι ο τρέχων παίκτης για το "YOU" tag
            Player currentPlayer = ((MainMenuActivity) getActivity()).getCurrentPlayer();
            if (currentPlayer != null &&
                    entry.getKey().equals(currentPlayer.getUsername())) {
                txtYou.setVisibility(View.VISIBLE);
                row.setBackgroundColor(getResources().getColor(R.color.gold_classic_transparent));
            } else {
                txtYou.setVisibility(View.GONE);
            }

            // Προσθήκη της γραμμής στο κεντρικό layout της κάρτας
            binding.layoutTopFive.addView(row);
            rank++;
        }

        // Αν η λίστα είναι άδεια, μπορείς να εμφανίσεις ένα μήνυμα
        if (topFive.isEmpty()) {
            TextView emptyTxt = new TextView(getContext());
            emptyTxt.setText(R.string.leaderboard_empty_msg);
            emptyTxt.setGravity(Gravity.CENTER);
            binding.layoutTopFive.addView(emptyTxt);
        }
    }

    @Override
    public void navigateToDeposit() {
        replaceFragmentWithAnimation(new DepositFragment());
    }

    @Override
    public void navigateToHistory() {
        replaceFragmentWithAnimation(new HistoryFragment());
    }

    @Override
    public void navigateToRatings() {
        replaceFragmentWithAnimation(new RateFragment());
    }

    @Override
    public void navigateToLogin() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.nav_logout_dialog_title)
                .setMessage(R.string.nav_logout_dialog_msg)
                .setIcon(R.drawable.ic_logout)
                .setPositiveButton(R.string.common_ok, (dialog, which) -> {

                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();

                })
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void navigateToSettings() {
        startActivity(new Intent(requireContext(), SettingsMenuActivity.class));
    }

    @Override
    public void navigateToStats() {
        replaceFragmentWithAnimation(new StatsFragment());
    }

    private void replaceFragmentWithAnimation(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // binding is nullified not to have memory leaks
        binding = null;
    }
}