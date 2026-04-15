package gr.goldenbet.view.settings.settingsMenu;

import android.content.SharedPreferences;

public class SettingsMenuPresenter {
    private SettingsMenuView view;
    private SharedPreferences prefs;

    public SettingsMenuPresenter(SettingsMenuView view, SharedPreferences prefs) {
        this.view = view;
        this.prefs = prefs;
    }

    public void start() {
        boolean isDark = prefs.getBoolean("is_dark_mode", true);
        view.showTheme(isDark);

        view.showLanguageName(gr.goldenbet.R.string.settings_selected_language_name);
    }

    public void onThemeToggled(boolean isDark) {
        prefs.edit().putBoolean("is_dark_mode", isDark).apply();
        view.applyThemeChange(isDark);
    }

    public void onLanguageSelected(String langCode) {
        view.applyLanguageChange(langCode);
    }

    public void onLanguageCardClicked() {
        view.navigateToLanguageSelection();
    }

}