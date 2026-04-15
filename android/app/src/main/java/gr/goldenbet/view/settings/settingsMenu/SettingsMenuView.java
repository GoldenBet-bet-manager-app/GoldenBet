package gr.goldenbet.view.settings.settingsMenu;

public interface SettingsMenuView {


    void showTheme(boolean isDark);

    void showLanguageName(int resId);

    void applyThemeChange(boolean isDark);

    void applyLanguageChange(String langCode);

    void navigateToLanguageSelection();
}