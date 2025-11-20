package com.example.swooshv2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class RunnerLogin extends AppCompatActivity {

    private EditText etRunnerID, etRPassword;
    private Button btnLogin;

    // Firestore reference
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference runnersRef = db.collection("Runners");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runner_login);

        // Initialize UI elements
        etRunnerID = findViewById(R.id.etRunnerID);
        etRPassword = findViewById(R.id.etRPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Set onClick listener for the Login button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginRunner();
            }
        });
    }

    private void loginRunner() {
        String runnerID = etRunnerID.getText().toString().trim();
        String password = etRPassword.getText().toString().trim();

        // Check if fields are empty
        if (TextUtils.isEmpty(runnerID)) {
            etRunnerID.setError("Runner ID is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etRPassword.setError("Password is required");
            return;
        }

        runnersRef.whereEqualTo("RunnerID", runnerID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (!querySnapshot.isEmpty()) {
                        // Retrieve the first matching document
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                        // Get the stored password
                        String storedPassword = document.getString("RPassword");

                        // Check if the entered password matches the stored password
                        if (password.equals(storedPassword)) {
                            Toast.makeText(RunnerLogin.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            // Redirect to another activity if needed
                            Intent intent = new Intent(RunnerLogin.this, RunnerDashboard.class);
                            intent.putExtra("RunnerID", runnerID);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RunnerLogin.this, "Incorrect password!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RunnerLogin.this, "Runner ID does not exist!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RunnerLogin.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
