package com.example.swooshv2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLogin extends AppCompatActivity {

    Button btnLoginLI;
    EditText etAEmail, etAPassw;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_login);

        btnLoginLI = findViewById(R.id.btnLoginLI);
        etAEmail = findViewById(R.id.etAEmail);
        etAPassw = findViewById(R.id.etAPassw);

        db = FirebaseFirestore.getInstance();

        btnLoginLI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String adminID = etAEmail.getText().toString().trim(); // Using etAEmail for AdminID
                String adminPassword = etAPassw.getText().toString().trim(); // Using etAPassw for AdminPassword

                if (TextUtils.isEmpty(adminID) || TextUtils.isEmpty(adminPassword)) {
                    Toast.makeText(AdminLogin.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Query Firestore for Admin credentials
                db.collection("Admins")
                        .whereEqualTo("AdminID", adminID)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                DocumentSnapshot adminDoc = task.getResult().getDocuments().get(0);
                                String storedPassword = adminDoc.getString("AdminPassword");

                                if (adminPassword.equals(storedPassword)) {
                                    Log.d("Firestore", "Admin login successful!");

                                    // Save login state
                                    saveLoginState(true);

                                    // Redirect to admin dashboard or main activity
                                    Intent intent = new Intent(AdminLogin.this, AdminDashboard.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Log.d("Firestore", "Invalid password!");
                                    Toast.makeText(AdminLogin.this, "Invalid password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.d("Firestore", "Admin ID not found!");
                                Toast.makeText(AdminLogin.this, "Admin ID not found", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    // Save login state to SharedPreferences
    private void saveLoginState(boolean isLoggedIn) {
        SharedPreferences sharedPreferences = getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isAdminLoggedIn", isLoggedIn); // Save the admin login state
        editor.apply();
    }
}
