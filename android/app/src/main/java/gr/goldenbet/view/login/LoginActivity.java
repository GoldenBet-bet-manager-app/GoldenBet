package gr.goldenbet.view.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import gr.goldenbet.R;
import gr.goldenbet.domain.Player;
import gr.goldenbet.utils.ConfigLoader;
import gr.goldenbet.view.mainMenu.MainMenuActivity;
import gr.goldenbet.view.register.RegisterActivity;
import gr.goldenbet.view.settings.settingsMenu.SettingsMenuActivity;

public class LoginActivity extends AppCompatActivity implements LoginView{

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button settingsButton;
    private TextView registerTextView;
    private LoginPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ConfigLoader.loadConfig(this, "master_network_config.conf");
        presenter = new LoginPresenter(this, getSharedPreferences("Settings", Context.MODE_PRIVATE));
        presenter.initializeSettings();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        usernameEditText = findViewById(R.id.edtloginUsername);
        passwordEditText = findViewById(R.id.edtloginPassword);
        loginButton = findViewById(R.id.loginButton);
        settingsButton = findViewById(R.id.btnloginSettings);
        registerTextView = findViewById(R.id.txtViewloginregister);



        settingsButton.setOnClickListener( v -> presenter.onSettings());

        registerTextView.setOnClickListener(v -> presenter.onRegister());

        loginButton.setOnClickListener(v ->{
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            presenter.onLogin(username, password);
        });



    }


    @Override
    public void navigateToAccount(Player player){
        runOnUiThread(()->{
            Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);

            intent.putExtra("PLAYER_DATA", player);
            startActivity(intent);
            finish();
        });

    }

    @Override
    public void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    public void navigateToSettings() {
        Intent intent = new Intent(LoginActivity.this, SettingsMenuActivity.class);
        startActivity(intent);
    }


    @Override
    public void onResume(){
        super.onResume();
        presenter.loginReset();
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public String getPassword() {
        return "";
    }


    @Override
    public void resetFields() {
        usernameEditText.setText("");
        passwordEditText.setText("");
    }

    @Override
    public void showErrorMessage(String message) {
        runOnUiThread(()-> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void showSuccessMessage(String message) {

    }

    @Override
    public boolean isSystemInDarkMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public String getSystemLanguage() {
        // Returns system language code (e.g. "el" ή "en")
        return java.util.Locale.getDefault().getLanguage();
    }

    @Override
    public void applyTheme(boolean isDark) {
        AppCompatDelegate.setDefaultNightMode(isDark ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    @Override
    public void applyLanguage(String langCode) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(langCode);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

}