package com.example.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    public static final String TAG = Register.class.getName();
    EditText nickNameField , emailField , passwordField;
    Button registerButton;
    TextView alreadyResigteredField;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nickNameField   = findViewById(R.id.nick_name);
        emailField      = findViewById(R.id.email);
        passwordField   = findViewById(R.id.password);
        registerButton= findViewById(R.id.login_button);
        alreadyResigteredField   = findViewById(R.id.already_resigtered);

        //Get connection from Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);

        if(fAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();
                final String nickName = nickNameField.getText().toString();

                if(TextUtils.isEmpty(nickName)){
                    nickNameField.setError("Please enter a nickname");
                    return;
                }

                if(TextUtils.isEmpty(email)){
                    emailField.setError("Please enter email");
                    return;
                }

                if(!Login.isValidEmailAddress(email)){
                    emailField.setError("Email format is not correct, Please check again");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    passwordField.setError("Please enter password");
                    return;
                }


                if(password.length() < 6){
                    passwordField.setError("The length of password should greater than 6");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // Create info of a user and insert data into firebase
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            // verification link will be sent
                            FirebaseUser tempUser = fAuth.getCurrentUser();
                            tempUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Register.this, "Verification has been sent to email you registered", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "Failure: error happened" + e.getMessage());
                                }
                            });

                            Toast.makeText(Register.this, "User generated successfully.", Toast.LENGTH_SHORT).show();
                            // Get current uid to as a login credential
                            userID = fAuth.getCurrentUser().getUid();
                            // Store user info into the table called Information
                            DocumentReference documentReference = fStore.collection("Information").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("nickname",nickName);
                            user.put("email",email);

                            // Dummy high score
                            user.put("high_score","100");

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Success: user info was makde successfully for "+ userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "Failure: " + e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));

                        }else {
                            Toast.makeText(Register.this, "Error happened" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });


        // Jump from RegisteredField to Login activity
        alreadyResigteredField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });

    }
}
