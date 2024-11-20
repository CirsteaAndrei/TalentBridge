package com.cst.talentbridge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "Firestore";
    private EditText fullNameField, emailField, passwordField, skillsField, universityField, facultyField;
    private Button signupButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Link UI elements
        fullNameField = findViewById(R.id.fullNameField);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        skillsField = findViewById(R.id.skillsField);
        universityField = findViewById(R.id.universityField);
        facultyField = findViewById(R.id.facultyField);
        signupButton = findViewById(R.id.signupButton);

        // Sign Up Button Click
        signupButton.setOnClickListener(v -> signUpUser());
    }

    private void signUpUser() {
        String fullName = fullNameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String skills = skillsField.getText().toString().trim();
        String university = universityField.getText().toString().trim();
        String faculty = facultyField.getText().toString().trim();

        // Validate input
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() ||
                skills.isEmpty() || university.isEmpty() || faculty.isEmpty()) {
            Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get the user ID
                        String userId = auth.getCurrentUser().getUid();

                        // Save user data in Firestore
                        Map<String, Object> student = new HashMap<>();
                        student.put("student_id", userId);
                        student.put("name", fullName);
                        student.put("skills", Arrays.asList(skills.split(","))); // Convert comma-separated skills to a list
                        student.put("university", university);
                        student.put("faculty", faculty);

                        db.collection("students")
                                .document(userId) // Use user ID as the document ID
                                .set(student)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SignupActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                                    // Redirect to Dashboard
                                    startActivity(new Intent(SignupActivity.this, DashboardActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Error adding document", e);
                                    Toast.makeText(SignupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(SignupActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
