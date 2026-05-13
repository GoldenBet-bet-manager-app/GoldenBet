package gr.goldenbet.view.mainMenu;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import gr.goldenbet.R;
import gr.goldenbet.databinding.ActivityMainMenuBinding;
import gr.goldenbet.domain.Player;
import gr.goldenbet.view.login.LoginActivity;
import gr.goldenbet.view.mainMenu.home.HomePresenter;
import gr.goldenbet.view.mainMenu.home.rate.RateFragment;
import gr.goldenbet.view.mainMenu.home.HomeFragment;
import gr.goldenbet.view.mainMenu.profile.ProfileFragment;
import gr.goldenbet.view.mainMenu.search.SearchFragment;

public class MainMenuActivity extends AppCompatActivity implements MainMenuView {

    private ActivityMainMenuBinding binding;
    private MainMenuPresenter presenter;
    private MainMenuViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });


        // ViewModel initialization
        viewModel = new ViewModelProvider(this).get(MainMenuViewModel.class);

        // If ViewModel player in null, it means we just logged in
        // If not, it means that rotation happened and the player was saved by ViewModel
        if (viewModel.getCurrentPlayer() == null) {
            Player playerFromIntent = (Player) getIntent().getSerializableExtra("PLAYER_DATA");
            viewModel.setCurrentPlayer(playerFromIntent);
        }

        presenter = new MainMenuPresenter(this);

        // UI Setup
        setupNavigation();

        // Διαχείρηση κουμπιού πίσω του κινητού
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                    return;
                }

                int currentTab = binding.bottomNavigationView.getSelectedItemId();

                if (currentTab == R.id.main_menu_search || currentTab == R.id.main_menu_profile ) {
                    binding.bottomNavigationView.setSelectedItemId(R.id.main_menu_home);
                } else {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if(currentFragment instanceof HomeFragment){

                        showExitConfirmationDialog(((HomeFragment) currentFragment).getPresenter());
                    }
                }
            }
        });

        if (savedInstanceState == null) {
            showHome();
        }
    }

    // When we want to change Fragment
    private void setupNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            presenter.onNavigationItemSelected(item.getItemId());
            return true;
        });
    }

    public void showExitConfirmationDialog(HomePresenter presenter) {

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.nav_logout_dialog_title)
                .setMessage(R.string.nav_logout_dialog_msg)
                .setIcon(R.drawable.ic_logout)
                .setPositiveButton(R.string.common_ok, (dialog, which) -> {

                    presenter.confirmLogout();
                    Intent intent = new Intent(MainMenuActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }


    // --- Implementation of MainMenuView ---
    @Override
    public void updateLocalPlayer(Player player) {
        viewModel.setCurrentPlayer(player);
    }

    @Override
    public Player getCurrentPlayer() {
        return viewModel.getCurrentPlayer();
    }


    // Fragments Logic
    @Override
    public void showHome() { replaceFragment(new HomeFragment()); }
    @Override
    public void showSearch() { replaceFragment(new SearchFragment()); }
    @Override
    public void showProfile() { replaceFragment(new ProfileFragment()); }
    @Override
    public void showRate() { replaceFragment(new RateFragment()); }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}