package gr.goldenbet.view.login;

import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.List;

public class LoginPresenter {

    private LoginView view;
    private SharedPreferences prefs;
    private final List<String> SUPPORTED_LANGUAGES = Arrays.asList("en", "el");
    private final String DEFAULT_LANGUAGE = "en";

    public LoginPresenter(LoginView view, SharedPreferences prefs) {
        this.view = view;
        this.prefs = prefs;
    }
    public void initializeSettings() {
        if (!prefs.contains("is_dark_mode")) {
            setupFirstRunDefaults();
        } else {
            applyStoredPreferences();
        }

        if (!prefs.contains("lang")) {
            setupFirstRunLanguage();
        } else {
            // Εφαρμογή της ήδη αποθηκευμένης
            view.applyLanguage(prefs.getString("lang", DEFAULT_LANGUAGE));
        }
    }

    private void setupFirstRunLanguage() {
        String systemLang = view.getSystemLanguage();

        String finalLang;
        // check whether phone's language is in SUPPORTED_LANGUAGES
        if (SUPPORTED_LANGUAGES.contains(systemLang)) {
            finalLang = systemLang;
        } else {
            // Otherwise the deafault language is selected
            finalLang = DEFAULT_LANGUAGE;
        }

        prefs.edit().putString("lang", finalLang).apply();
        view.applyLanguage(finalLang);
    }

    private void setupFirstRunDefaults() {
        boolean systemDark = view.isSystemInDarkMode();
        String systemLang = view.getSystemLanguage();

        String finalLang = systemLang.startsWith("el") ? "el" : "en";

        prefs.edit()
                .putBoolean("is_dark_mode", systemDark)
                .putString("lang", finalLang)
                .apply();

        view.applyTheme(systemDark);
        view.applyLanguage(finalLang);
    }

    private void applyStoredPreferences() {
        boolean isDark = prefs.getBoolean("is_dark_mode", true);
        view.applyTheme(isDark);

        // Language is applied automatically
    }
    public void onRegister() {
        view.navigateToRegister();
    }

    public void onSettings() {view.navigateToSettings();}


    public void loginReset(){
        view.resetFields();
    }
}
