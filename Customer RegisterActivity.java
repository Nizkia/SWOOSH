package com.example.myapplication;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private EditText editTextTelephone;
    private CheckBox checkboxTerms;
    private Button buttonSignUp;
    private ImageView passwordToggle;
    private ImageView confirmPasswordToggle;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextTelephone = findViewById(R.id.editTextTelephone);
        checkboxTerms = findViewById(R.id.checkboxTerms);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        passwordToggle = findViewById(R.id.passwordToggle);
        confirmPasswordToggle = findViewById(R.id.confirmPasswordToggle);

        // Set click listener for password visibility toggle
        passwordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide password
                    editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passwordToggle.setImageResource(R.drawable.ic_visibility); // Replace with your visibility icon
                    isPasswordVisible = false;
                } else {
                    // Show password
                    editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passwordToggle.setImageResource(R.drawable.ic_visibility_off); // Replace with your visibility off icon
                    isPasswordVisible = true;
                }
                // Move cursor to the end of the input field
                editTextPassword.setSelection(editTextPassword.getText().length());
            }
        });

        // Set click listener for confirm password visibility toggle
        confirmPasswordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConfirmPasswordVisible) {
                    // Hide password
                    editTextConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    confirmPasswordToggle.setImageResource(R.drawable.ic_visibility); // Replace with your visibility icon
                    isConfirmPasswordVisible = false;
                } else {
                    // Show password
                    editTextConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    confirmPasswordToggle.setImageResource(R.drawable.ic_visibility_off); // Replace with your visibility off icon
                    isConfirmPasswordVisible = true;
                }
                // Move cursor to the end of the input field
                editTextConfirmPassword.setSelection(editTextConfirmPassword.getText().length());
            }
        });

        // Set click listener for the sign-up button
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();
                String phone = editTextTelephone.getText().toString().trim();

                // Check if any field is empty
                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if password and confirm password match
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if terms and conditions are accepted
                if (!checkboxTerms.isChecked()) {
                    Toast.makeText(RegisterActivity.this, "You must agree to the terms and conditions", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Placeholder for actual registration logic (e.g., API call or database storage)
                Toast.makeText(RegisterActivity.this, "Registration feature not implemented yet", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
