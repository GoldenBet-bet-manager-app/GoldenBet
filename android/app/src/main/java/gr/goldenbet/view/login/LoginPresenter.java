package gr.goldenbet.view.login;

import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.List;

import gr.goldenbet.R;
import gr.goldenbet.domain.Client;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.Player;
import gr.goldenbet.view.register.RegisterActivity;

public class LoginPresenter {

    private LoginView view;
    private SharedPreferences prefs;
    private final List<String> SUPPORTED_LANGUAGES = Arrays.asList("en", "el");
    private final String DEFAULT_LANGUAGE = "en";

    // Master's network configurations
    private final String MASTER_IP;
    private final int MASTER_PORT;

    public LoginPresenter(LoginView view, SharedPreferences prefs) {
        this.view = view;
        this.prefs = prefs;

        // Set master configurations
        MASTER_IP = ((RegisterActivity)view).getResources().getString(R.string.master_ip);
        MASTER_PORT = ((RegisterActivity)view).getResources().getInteger(R.integer.master_port);

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


    public void onLogin(String username, String password) {
        String[] credentials = new String[]{username, password};
        Client loginClient = new Client ( MASTER_IP, MASTER_PORT, new MasterMessage( MasterMessageType.LOGIN, credentials ) );
        loginClient.start();

        try {
            loginClient.join ( );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Object response = loginClient.getResponse ( );

        // Successful login
        if (response instanceof Player) {
            Player loggedInPlayer = (Player) response;
            view.navigateToAccount(loggedInPlayer.getPlayerId());
        }
        // failed login attempt (null is returned)
        else{
            String msg = ((LoginActivity) view).getResources().getString(R.string.login_failure);
            view.showErrorMessage(msg);
        }
    }


    public void onRegister() {
        view.navigateToRegister();
    }

    public void onSettings() {view.navigateToSettings();}


    public void loginReset(){
        view.resetFields();
    }
}
