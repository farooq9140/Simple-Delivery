package com.example.delivery_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class Forget_Password extends AppCompatActivity
implements View.OnClickListener{

    private EditText email;
    private Button sendEmail;
    private TextView goBack;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        email = findViewById(R.id.email1);

        goBack = findViewById(R.id.goBack);
        goBack.setOnClickListener(this);

        sendEmail = findViewById(R.id.sendEmail);
        sendEmail.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sendEmail:
                resetPassword();
            case R.id.goBack:
                startActivity(new Intent(this,MainActivity.class));
        }
    }

    private void resetPassword(){
        String emailAddress = email.getText().toString().trim();

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

        mAuth.sendPasswordResetEmail(emailAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(Forget_Password.this, "Check your email to reset your password", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(Forget_Password.this, "Something wrong, try again", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}