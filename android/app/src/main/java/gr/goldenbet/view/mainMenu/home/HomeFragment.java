package gr.goldenbet.view.mainMenu.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import gr.goldenbet.R;
import gr.goldenbet.databinding.FragmentHomeBinding;
import gr.goldenbet.view.login.LoginActivity;
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

        // Αρχικοποίηση του Presenter
        presenter = new HomePresenter(this);

        setupClickListeners();

        presenter.initData();
    }

    private void setupClickListeners() {
        binding.homeCardDeposit.setOnClickListener(v -> presenter.onDepositClicked());
        binding.homeCardHistory.setOnClickListener(v -> presenter.onHistoryClicked());
        binding.homeCardStats.setOnClickListener(v -> presenter.onStatsClicked());
        binding.homeCardRatings.setOnClickListener(v -> presenter.onRatingsClicked());
        binding.homeBtnLogout.setOnClickListener( v -> presenter.onLogoutCLicked());
        binding.homeBtnSettings.setOnClickListener(v -> presenter.onSettingsClicked());
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


        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,  // enter
                        R.anim.slide_out_left,  // exit
                        R.anim.slide_in_left,   // popEnter
                        R.anim.slide_out_right  // popExit
                )
                .replace(R.id.fragment_container, new DepositFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void navigateToHistory() {


        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,  // enter
                        R.anim.slide_out_left,  // exit
                        R.anim.slide_in_left,   // popEnter
                        R.anim.slide_out_right  // popExit
                )
                .replace(R.id.fragment_container, new StatsFragment())
                .addToBackStack(null)
                .commit();
        Toast.makeText(getContext(), R.string.nav_history_msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToStats() {
        Toast.makeText(getContext(), R.string.nav_stats_msg, Toast.LENGTH_SHORT).show();    }


    @Override
    public void navigateToRatings() {

        Toast.makeText(getContext(), R.string.nav_ratings_msg, Toast.LENGTH_SHORT).show();
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.fragment_container, new RateFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void navigateToLogin(){
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.nav_logout_dialog_title)
                .setMessage(R.string.nav_logout_dialog_msg)
                .setIcon(R.drawable.ic_logout)
                .setPositiveButton(R.string.common_ok, (dialog, which) -> {
                    Intent intent = new Intent(requireContext(), LoginActivity.class);

                    // clean back stack not to return with back button
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);

                    requireActivity().finish();
                })
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();

    }

    @Override
    public void navigateToSettings(){
        // requireContext() is used to retrieve the context of the host activity
        Intent intent = new Intent(requireContext(), SettingsMenuActivity.class);
        startActivity(intent);
        Toast.makeText(getContext(), R.string.nav_settings_msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // binding is nullified not to have memory leaks
        binding = null;
    }
}