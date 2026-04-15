package gr.goldenbet.view.login;

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
     * @param title   The title of the error.
     * @param message The detailed error description.
     */
    void showErrorMessage(String title, String message);

    /**
     * Displays a success feedback message.
     * @param message The success description.
     */
    void showSuccessMessage(String message);

    /** Navigates to the user registration screen. */
    void navigateToRegister();

    /** Navigates to the user settings screen. */
    void navigateToSettings();


    boolean isSystemInDarkMode();

    String getSystemLanguage();

    void applyTheme(boolean isDark);

    void applyLanguage(String langCode);

}
