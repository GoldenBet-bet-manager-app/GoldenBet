package gr.goldenbet.view.settings.settingsMenu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;

import gr.goldenbet.R;
import gr.goldenbet.view.settings.LanguageList.LanguageListActivity;

public class SettingsMenuActivity extends AppCompatActivity implements SettingsMenuView {

    private MaterialSwitch switchDarkMode;
    private MaterialCardView cardLanguage;
    private TextView txtCurrentLanguage;
    private SettingsMenuPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings_menu);

        // UI Setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        switchDarkMode = findViewById(R.id.SettingsswitchDarkMode);
        cardLanguage = findViewById(R.id.SettingsCardLanguage);
        txtCurrentLanguage = findViewById(R.id.txtCurrentLanguage);

        // Initializing Presenter
        presenter = new SettingsMenuPresenter(this, getSharedPreferences("Settings", Context.MODE_PRIVATE));

        // Start Logic
        presenter.start();

        // UI Listeners -> Delegate to Presenter
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            presenter.onThemeToggled(isChecked);
        });

        cardLanguage.setOnClickListener(v -> {
            presenter.onLanguageCardClicked();
        });
    }

    // --- SettingsMenuView Implementation Methods ---

    @Override
    public void showTheme(boolean isDark) {
        switchDarkMode.setChecked(isDark);
    }

    @Override
    public void showLanguageName(int resId) {
        txtCurrentLanguage.setText(getString(resId));
    }

    @Override
    public void applyThemeChange(boolean isDark) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public void applyLanguageChange(String langCode) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(langCode);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }


    @Override
    public void navigateToLanguageSelection() {
        Intent intent = new Intent(this, LanguageListActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.start();
    }


}