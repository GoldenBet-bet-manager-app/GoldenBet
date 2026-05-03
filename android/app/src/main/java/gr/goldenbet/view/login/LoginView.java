package gr.goldenbet.view.login;

import java.util.concurrent.atomic.AtomicInteger;

import gr.goldenbet.domain.Player;

public interface LoginView {

    /**
     * @return username input.
     */
    String getUsername();

    /**
     * @return password input.
     */
    String getPassword();

    /**
     * Clears the input fields for username and password.
     */
    void resetFields();

    /**
     * Displays an error alert dialog.
     * @param message The detailed error description.
     */
    void showErrorMessage(String message);

    /**
     * Displays a success feedback message.
     * @param message The success description.
     */
    void showSuccessMessage(String message);

    /** Navigates to the user registration screen. */
    void navigateToRegister();

    /** Navigates to the user's account home screen*/
    void navigateToAccount(Player player);

    /** Navigates to the user settings screen. */
    void navigateToSettings();


    boolean isSystemInDarkMode();

    String getSystemLanguage();

    void applyTheme(boolean isDark);

    void applyLanguage(String langCode);

}
