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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class CustomerLogin extends AppCompatActivity {

    TextView tvRegLI, tvLogAd,tvRegrun;
    Button btnLoginLI;
    EditText etEmailLI, etPasswordLI;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_login);

        tvRegLI = findViewById(R.id.tvRegLI);
        tvRegrun = findViewById(R.id.tvRegrun);
        tvLogAd = findViewById(R.id.tvloginAd);
        btnLoginLI = findViewById(R.id.btnLoginLI);
        etEmailLI = findViewById(R.id.etAEmail);
        etPasswordLI = findViewById(R.id.etAPassw);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Navigate to registration screen
        tvRegLI.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CustomerRegister.class);
            startActivity(intent);
            finish();
        });

        // Navigate to registration screen
        tvRegrun.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), RunnerRegister.class);
            startActivity(intent);
            finish();
        });

        tvLogAd.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AdminLogin.class);
            startActivity(intent);
            finish();
        });

        // Login button click listener
        btnLoginLI.setOnClickListener(v -> {
            String email = etEmailLI.getText().toString().trim();
            String password = etPasswordLI.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(CustomerLogin.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Authenticate user
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("FirebaseAuth", "User signed in successfully.");
                    String userEmail = auth.getCurrentUser().getEmail();
                    fetchUserData(userEmail);
                } else {
                    Log.e("FirebaseAuth", "Login failed: " + task.getException().getMessage());
                    Toast.makeText(CustomerLogin.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });


    }

    // Fetch user data from Firestore
    private void fetchUserData(String userEmail) {
        db.collection("Users").document(userEmail).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                Log.d("Firestore", "Document Snapshot: " + document.getData()); // Log the entire snapshot
                String userName = document.getString("UName");

                if (userName != null) {
                    Log.d("Firestore", "User data fetched successfully. UName: " + userName);

                    // Save login state and user data
                    saveLoginState(true, userName);

                    // Pass the username to MainActivity
                    Intent intent = new Intent(CustomerLogin.this, MainActivity.class);
                    intent.putExtra("UName", userName);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e("Firestore", "UName field is null.");
                    Toast.makeText(this, "Failed to fetch user name.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("Firestore", "Error fetching user data.", task.getException());
                Toast.makeText(CustomerLogin.this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    // Save login state and user name to SharedPreferences
    private void saveLoginState(boolean isLoggedIn, String userName) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", isLoggedIn); // Save login state
        editor.putString("UName", userName); // Save user name
        editor.apply();
    }
}
