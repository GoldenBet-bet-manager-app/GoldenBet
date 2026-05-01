package gr.goldenbet.view.register;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import gr.goldenbet.R;
import gr.goldenbet.domain.Player;

import android.app.AlertDialog;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity implements RegisterView {
    private RegisterPresenter presenter;
    private TextInputEditText edtName, edtSurname, edtUsername, edtPassword,
    edtEmail, edtPhone, edtStreet, edtStreetNumber, edtCity, edtZipCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        presenter = new RegisterPresenter(this);

        edtUsername = findViewById(R.id.edtRegisterUsername);
        edtPassword = findViewById(R.id.edtRegisterPassword);
        edtName = findViewById(R.id.edtRegisterName);
        edtSurname = findViewById(R.id.edtRegisterSurname);
        edtEmail = findViewById(R.id.edtRegisterEmail);
        edtPhone = findViewById(R.id.edtRegisterPhone);
        edtStreet = findViewById(R.id.edtRegisterStreet);
        edtStreetNumber = findViewById(R.id.edtRegisterStreetNumber);
        edtCity = findViewById(R.id.edtRegisterCity);
        edtZipCode = findViewById(R.id.edtRegisterZip);

        Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            presenter.onRegisterClick(
                    edtUsername.getText().toString(), edtPassword.getText().toString(),
                    edtName.getText().toString(), edtSurname.getText().toString(),
                    edtEmail.getText().toString(), edtPhone.getText().toString(),
                    edtStreet.getText().toString(),edtStreetNumber.getText().toString(),
                    edtCity.getText().toString(), edtZipCode.getText().toString()
            );
        });
    }

    @Override
    public void showConfirmDialog(Player player) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_title)
                .setMessage(R.string.confirm_msg)
                .setPositiveButton(R.string.common_ok, (dialog, which) -> {
                    presenter.sendRegistrationToMaster(player);
                })
                .setNegativeButton(R.string.common_cancel, null)
                .show();
    }

    @Override
    public void onRegisterSuccess() {

        // Switch to Main Thread to update the UI
        runOnUiThread(() -> {
            Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRegisterError(String message) {

        // Switch to Main Thread to update the UI
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }
}