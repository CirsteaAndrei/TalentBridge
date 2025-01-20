package com.cst.talentbridge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "Firestore";

    // Common Fields
    private EditText emailField, passwordField, companyEmailField, companyPasswordField;

    // Student-Specific Fields
    private EditText fullNameField, universityField, facultyField;
    private Spinner skillsDropdown;
    private LinearLayout selectedSkillsContainer;

    // Company-Specific Fields
    private EditText companyNameField, descriptionField, locationField;

    private Button signupButton;
    private RadioGroup accountTypeRadioGroup;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private List<String> skillsList = new ArrayList<>();
    private List<String> selectedSkills = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Link Common UI elements
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        signupButton = findViewById(R.id.signupButton);
        accountTypeRadioGroup = findViewById(R.id.accountTypeRadioGroup);

        companyEmailField = findViewById(R.id.companyEmailField);
        companyPasswordField = findViewById(R.id.companyPasswordField);

        // Link Student-Specific Fields
        fullNameField = findViewById(R.id.fullNameField);
        universityField = findViewById(R.id.universityField);
        facultyField = findViewById(R.id.facultyField);
        skillsDropdown = findViewById(R.id.skillsDropdown);
        selectedSkillsContainer = findViewById(R.id.selectedSkillsContainer);

        // Link Company-Specific Fields
        companyNameField = findViewById(R.id.companyNameField);
        descriptionField = findViewById(R.id.descriptionField);
        locationField = findViewById(R.id.locationField);

        // Handle RadioGroup Changes
        accountTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.studentRadioButton) {
                // Show student fields and hide company fields
                findViewById(R.id.studentForm).setVisibility(View.VISIBLE);
                findViewById(R.id.companyForm).setVisibility(View.GONE);
            } else if (checkedId == R.id.companyRadioButton) {
                // Show company fields and hide student fields
                findViewById(R.id.studentForm).setVisibility(View.GONE);
                findViewById(R.id.companyForm).setVisibility(View.VISIBLE);
            }
        });

        // Populate skills dropdown
        loadSkillsFromFirestore();

        // Handle skill selection
        skillsDropdown.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedSkill = skillsList.get(position);
                if (!selectedSkill.equals("Select Skill") && !selectedSkills.contains(selectedSkill)) {
                    selectedSkills.add(selectedSkill);
                    addSkillChip(selectedSkill);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Sign Up Button Click
        signupButton.setOnClickListener(v -> signUpUser());
    }

    private void loadSkillsFromFirestore() {
        db.collection("skills")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    skillsList.clear();
                    skillsList.add("Select Skill"); // Default option
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String skill = document.getString("name");
                        if (skill != null) {
                            skillsList.add(skill);
                        }
                    }

                    // Set up the spinner adapter
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            R.layout.spinner_item,
                            skillsList
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    skillsDropdown.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignupActivity.this, "Failed to load skills. Please try again later.", Toast.LENGTH_SHORT).show();
                });
    }

    private void addSkillChip(String skill) {
        // Inflate the chip layout
        View chip = LayoutInflater.from(this).inflate(R.layout.skill_chip, selectedSkillsContainer, false);

        TextView skillName = chip.findViewById(R.id.skillName);
        skillName.setText(skill);

        View removeButton = chip.findViewById(R.id.removeSkillButton);
        removeButton.setOnClickListener(v -> {
            // Remove the skill from selected list and UI
            selectedSkills.remove(skill);
            selectedSkillsContainer.removeView(chip);
        });

        selectedSkillsContainer.addView(chip);
    }

    private void signUpUser() {
        // Get Common Data
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        String companyEmail = companyEmailField.getText().toString().trim();
        String companyPassword = companyPasswordField.getText().toString().trim();

        // Check Account Type
        int selectedAccountType = accountTypeRadioGroup.getCheckedRadioButtonId();
        if (selectedAccountType == R.id.studentRadioButton) {
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Please fill email and password for student", Toast.LENGTH_SHORT).show();
                return;
            }
            signUpStudent(email, password);
        } else if (selectedAccountType == R.id.companyRadioButton) {
            if (companyEmail.isEmpty() || companyPassword.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Please fill email and password for company", Toast.LENGTH_SHORT).show();
                return;
            }
            signUpCompany(companyEmail, companyPassword);
        } else {
            Toast.makeText(SignupActivity.this, "Please select an account type", Toast.LENGTH_SHORT).show();
        }
    }

    private void signUpStudent(String email, String password) {
        // Get Student-Specific Data
        String fullName = fullNameField.getText().toString().trim();
        String university = universityField.getText().toString().trim();
        String faculty = facultyField.getText().toString().trim();

        // Validate Student Fields
        if (fullName.isEmpty() || selectedSkills.isEmpty() || university.isEmpty() || faculty.isEmpty()) {
            Toast.makeText(SignupActivity.this, "Please fill all student fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Student User in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();

                        // Save Student Details in Firestore
                        Map<String, Object> student = new HashMap<>();
                        student.put("student_id", userId);
                        student.put("name", fullName);
                        student.put("skills", selectedSkills);
                        student.put("university", university);
                        student.put("faculty", faculty);

                        db.collection("students")
                                .document(userId)
                                .set(student)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SignupActivity.this, "Student Account Created", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SignupActivity.this, DashboardActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Error adding student document", e);
                                    Toast.makeText(SignupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(SignupActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signUpCompany(String email, String password) {
        // Get Company-Specific Data
        String companyName = companyNameField.getText().toString().trim();
        String description = descriptionField.getText().toString().trim();
        String location = locationField.getText().toString().trim();

        // Validate Company Fields
        if (companyName.isEmpty() || description.isEmpty() || location.isEmpty()) {
            Toast.makeText(SignupActivity.this, "Please fill all company fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Company User in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = auth.getCurrentUser().getUid();

                        // Save Company Details in Firestore
                        Map<String, Object> company = new HashMap<>();
                        company.put("company_id", userId);
                        company.put("name", companyName);
                        company.put("description", description);
                        company.put("location", location);
                        company.put("email", email);

                        db.collection("companies")
                                .document(userId)
                                .set(company)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(SignupActivity.this, "Company Account Created", Toast.LENGTH_SHORT).show();

                                    // Redirect to Company Dashboard
                                    Intent intent = new Intent(SignupActivity.this, CompanyDashboardActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Error adding company document", e);
                                    Toast.makeText(SignupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(SignupActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
