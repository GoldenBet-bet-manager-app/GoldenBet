package gr.goldenbet.view.register;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import gr.goldenbet.R;
import gr.goldenbet.domain.Player;

import android.app.AlertDialog;
import android.os.Environment;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity implements RegisterView {
    private RegisterPresenter presenter;
    private TextInputEditText edtName, edtSurname, edtUsername, edtPassword,
    edtEmail, edtPhone, edtStreet, edtStreetNumber, edtCity, edtZipCode;

    private ImageView imgProfile;
    private byte[] profileImageBytes = null;
    private Uri photoUri;


    // Camera Launcher
    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), result -> {
                if (result) {
                    handleImageSelection(photoUri);
                }
            });


    // Camera permission request Launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(this, "Η άδεια κάμερας είναι απαραίτητη για τη λήψη φωτογραφίας", Toast.LENGTH_SHORT).show();
                }
            });



    // Gallery Launcher
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    handleImageSelection(uri);
                }
            });

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

        imgProfile = findViewById(R.id.imgProfile);

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
        TextView btnSignin = findViewById(R.id.registerSignInTextView);
        TextView btnChangePhoto = findViewById(R.id.btnChangePhoto);


        btnChangePhoto.setOnClickListener(v -> showImagePickerDialog());

        btnRegister.setOnClickListener(v -> {
            presenter.onRegisterClick(
                    edtUsername.getText().toString(), edtPassword.getText().toString(),
                    edtName.getText().toString(), edtSurname.getText().toString(),
                    edtEmail.getText().toString(), edtPhone.getText().toString(),
                    edtStreet.getText().toString(), edtStreetNumber.getText().toString(),
                    edtCity.getText().toString(), edtZipCode.getText().toString(),
                    profileImageBytes
            );
        });


        btnSignin.setOnClickListener(v->{
            this.navigateToLogin();
        });

    }

    private void showImagePickerDialog() {
        String[] options = {
                getString(R.string.register_image_option_camera),
                getString(R.string.register_image_option_gallery)
        };
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.register_image_picker_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {

                        // Camera Permission Check
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            openCamera();
                        } else {
                            // Permission Request
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
                        }
                    } else {
                        galleryLauncher.launch("image/*");
                    }

                }).show();
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(photoUri);
        } catch (IOException ex) {
            onRegisterError(getString(R.string.register_image_error_camera, ex.getMessage()));
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("IMG_" + timeStamp + "_", ".webp", storageDir);
    }

    private void handleImageSelection(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            imgProfile.setImageBitmap(bitmap);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            // Compression
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 75, stream);
            } else {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 75, stream);
            }

            profileImageBytes = stream.toByteArray();

        } catch (IOException e) {
            onRegisterError(getString(R.string.register_image_error_processing));
        }
    }


    @Override
    public void showConfirmDialog(Player player) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_title)
                .setMessage(R.string.confirm_msg)
                .setIcon(R.drawable.ic_person_add)
                .setPositiveButton(R.string.common_ok, (dialog, which) -> {

                    presenter.sendRegistrationToMaster(player);
                })
                .setNegativeButton(R.string.common_cancel, (dialog, which) -> dialog.dismiss())
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

    @Override
    public void navigateToLogin(){
        finish();
    }
}