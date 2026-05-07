package gr.goldenbet.view.login;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import gr.goldenbet.R;
import gr.goldenbet.domain.MasterMessage;
import gr.goldenbet.domain.MasterMessageType;
import gr.goldenbet.domain.Player;


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
        MASTER_IP = ((LoginActivity)view).getResources().getString(R.string.master_ip);
        MASTER_PORT = ((LoginActivity)view).getResources().getInteger(R.integer.master_port);

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

        MasterMessage loginMsg = new MasterMessage(MasterMessageType.LOGIN, credentials);
        Gson gson = new Gson();
        String jsonToSend = gson.toJson(loginMsg);


        new Thread(() -> {
            try (Socket socket = new Socket(MASTER_IP, MASTER_PORT);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {


                // Message sent to Master
                out.writeObject(jsonToSend);
                out.flush();


                // Master's response
                String masterReply = (String) in.readObject();
                JsonObject responseJson = gson.fromJson(masterReply, JsonObject.class);
                String status = responseJson.get("status").getAsString();

                ((Activity) view).runOnUiThread(() -> {
                    if ("SUCCESS".equals(status)) {
                        Player loggedInPlayer = gson.fromJson(responseJson.get("loggedInPlayer"), Player.class);

                        view.navigateToAccount(loggedInPlayer);
                    } else {
                        // Σφάλμα από τον Master
                        String errorMsg = responseJson.get("message").getAsString();
                        view.showErrorMessage(errorMsg);
                    }
                });
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void onRegister() {
        view.navigateToRegister();
    }

    public void onSettings() {view.navigateToSettings();}


    public void loginReset(){
        view.resetFields();
    }
}
