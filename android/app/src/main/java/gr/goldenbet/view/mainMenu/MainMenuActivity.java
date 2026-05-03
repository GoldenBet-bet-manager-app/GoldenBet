package gr.goldenbet.view.mainMenu;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import gr.goldenbet.R;
import gr.goldenbet.databinding.ActivityMainMenuBinding;
import gr.goldenbet.domain.Player;
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
        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
    @Override public void showHome() { replaceFragment(new HomeFragment()); }
    @Override public void showSearch() { replaceFragment(new SearchFragment()); }
    @Override public void showProfile() { replaceFragment(new ProfileFragment()); }
    @Override public void showRate() { replaceFragment(new RateFragment()); }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}