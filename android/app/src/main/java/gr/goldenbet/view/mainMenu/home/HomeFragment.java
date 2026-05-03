package gr.goldenbet.view.mainMenu.home;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import gr.goldenbet.R;
import gr.goldenbet.databinding.FragmentHomeBinding;
import gr.goldenbet.domain.Player;
import gr.goldenbet.view.login.LoginActivity;
import gr.goldenbet.view.mainMenu.MainMenuActivity;
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
    }

    private void setupClickListeners() {
        binding.homeCardDeposit.setOnClickListener(v -> presenter.onDepositClicked());
        binding.homeCardHistory.setOnClickListener(v -> presenter.onHistoryClicked());
        binding.homeCardStats.setOnClickListener(v -> presenter.onStatsClicked());
        binding.homeCardRatings.setOnClickListener(v -> presenter.onRatingsClicked());
        binding.homeBtnLogout.setOnClickListener( v -> presenter.onLogoutClicked());
        binding.homeBtnSettings.setOnClickListener(v -> presenter.onSettingsClicked());
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
    public void navigateToDeposit() {
        replaceFragmentWithAnimation(new DepositFragment());
    }

    @Override
    public void navigateToHistory() {
        replaceFragmentWithAnimation(new StatsFragment());
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
        Toast.makeText(getContext(), R.string.nav_stats_msg, Toast.LENGTH_SHORT).show();
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