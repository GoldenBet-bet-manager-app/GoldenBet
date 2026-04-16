package gr.goldenbet.view.mainMenu;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import gr.goldenbet.R;
import gr.goldenbet.databinding.ActivityMainMenuBinding;
import gr.goldenbet.view.mainMenu.home.HomeFragment;
import gr.goldenbet.view.mainMenu.profile.ProfileFragment;
import gr.goldenbet.view.mainMenu.search.SearchFragment;

public class MainMenuActivity extends AppCompatActivity implements MainMenuView {

    private ActivityMainMenuBinding binding;
    private MainMenuPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        showHome();

        presenter = new MainMenuPresenter(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState == null) {
            showHome();
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            presenter.onNavigationItemSelected(item.getItemId());
            return true;
        });
    }

    @Override
    public void showHome() {
        replaceFragment(new HomeFragment());
    }

    @Override
    public void showSearch() {
        replaceFragment(new SearchFragment());
    }

    @Override
    public void showProfile() {
        replaceFragment(new ProfileFragment());
    }
    private void replaceFragment(Fragment fragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();

    }

}