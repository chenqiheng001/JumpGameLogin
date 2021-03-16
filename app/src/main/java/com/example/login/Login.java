package com.example.login;

import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {
    EditText emailField,passwordField;
    Button LoginButton;
    TextView alreadyRegistered, forgetLink;
    ProgressBar progressBar;
    FirebaseAuth fAuth;
    // Email format validate regex
    private static final String regex = "^(.+)@(.+)$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailField = findViewById(R.id.email);
        passwordField = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        fAuth = FirebaseAuth.getInstance();
        LoginButton = findViewById(R.id.login_button);
        alreadyRegistered = findViewById(R.id.already_resigtered);
        forgetLink = findViewById(R.id.forget_password);

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = emailField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    emailField.setError("Please enter your email");
                    return;
                }

                if(!isValidEmailAddress(email)){
                    emailField.setError("Email format is not correct, Please check again");
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    passwordField.setError("Please enter your password");
                    return;
                }

                if(password.length() < 6){
                    passwordField.setError("The length of password should greater than 6");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);


                fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Login.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }else {
                            Toast.makeText(Login.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }

                    }
                });

            }
        });

        alreadyRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Register.class));
            }
        });

        forgetLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("resetPasswordTag", "This is on click of reset password");
                final EditText resetText = new EditText(view.getContext());
                final AlertDialog.Builder resetDialog = new AlertDialog.Builder(view.getContext());
                resetDialog.setTitle("Do you want to reset password ?");
                resetDialog.setMessage("Please enter the email of your account");
                resetDialog.setView(resetText);


                resetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String mailString = resetText.getText().toString();
                        if(!isValidEmailAddress(mailString) || mailString.length() <= 0){
                            Toast.makeText(Login.this, "Error the email is not valid", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        fAuth.sendPasswordResetEmail(mailString).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Login.this, "Reset Link sent to email you entered", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Error happened! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                resetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Empty for closing
                    }
                });
                resetDialog.create().show();

            }
        });

    }

    public static boolean isValidEmailAddress(String email) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
