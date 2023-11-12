package com.example.delivery_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

public class Register extends AppCompatActivity
implements View.OnClickListener{

    private FirebaseAuth mAuth;

    private EditText email, password, confirmation,name;
    private Button signUp;
    private TextView logIn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmation = findViewById(R.id.confirmation);

        signUp = findViewById(R.id.signUp);
        signUp.setOnClickListener(this);

        logIn = findViewById(R.id.logIn);
        logIn.setOnClickListener(this);

        progressBar = findViewById(R.id.progressbar);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.logIn:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.signUp:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        String namee = name.getText().toString();
        String emailAddress = email.getText().toString().trim();
        String userPassword = password.getText().toString();
        String userConfirmation = confirmation.getText().toString();

        if(namee.isEmpty()){
            name.setError("email can not be empty!");
            name.requestFocus();
            return;
        }

        if(emailAddress.isEmpty()){
            email.setError("email can not be empty!");
            email.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()){
            email.setError("please provide valid email");
            email.requestFocus();
            return;
        }

        if(userPassword.isEmpty()){
            password.setError("password can not be empty!");
            password.requestFocus();
            return;
        }

        if(userPassword.length()<6){
            password.setError("The length should be at least 6!");
            password.requestFocus();
            return;
        }

        if(userConfirmation.isEmpty()){
            confirmation.setError("password can not be empty!");
            confirmation.requestFocus();
            return;
        }

        if (!userConfirmation.equals(userPassword)) {
            password.setError("The two password is not the same!");
            password.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(emailAddress, userPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            SharedPreferences.Editor editor = getSharedPreferences("name", MODE_PRIVATE).edit();
                            editor.putString("name", namee);
                            editor.commit();

                            User user = new User(emailAddress);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Intent goToScheduled_Delivery = new Intent( Register.this, MainActivity.class);
                                        startActivity(goToScheduled_Delivery);
                                        Toast.makeText(Register.this, "User has been registered successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(Register.this, "Fail to register, try again", Toast.LENGTH_SHORT).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                        else{
                            Toast.makeText(Register.this, "The account has already been registered", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }
}