package com.example.myapplication;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imageProfile;
    private ImageButton buttonChangePhoto;
    private TextInputEditText editTextFullName;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPhone;
    private TextInputEditText editTextStudentId;
    private MaterialButton buttonSave;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initializeViews();
        setupToolbar();
        setupClickListeners();
        loadUserData();
    }

    private void initializeViews() {
        imageProfile = findViewById(R.id.imageProfile);
        buttonChangePhoto = findViewById(R.id.buttonChangePhoto);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextStudentId = findViewById(R.id.editTextStudentId);
        buttonSave = findViewById(R.id.buttonSave);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupClickListeners() {
        buttonChangePhoto.setOnClickListener(v -> {
            // Implement photo selection logic here
            Toast.makeText(this, "Change photo clicked", Toast.LENGTH_SHORT).show();
        });

        buttonSave.setOnClickListener(v -> saveProfile());
    }

    private void loadUserData() {
        // Here you would typically load the user's current data
        // For now, we'll just set some dummy data
        editTextFullName.setText("John Doe");
        editTextEmail.setText("johndoe@example.com");
        editTextPhone.setText("+1234567890");
        editTextStudentId.setText("ST12345");
    }

    private void saveProfile() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String studentId = editTextStudentId.getText().toString().trim();

        // Validate inputs
        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || studentId.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Here you would typically save the data to your backend
        // For now, we'll just show a success message
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
