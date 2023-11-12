package com.example.delivery_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity
implements View.OnClickListener{

    private EditText email, password;
    private Button logIn;
    private TextView forgetPassword, register;
    CheckBox remember;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.emailAddress);
        password = findViewById(R.id.password1);
        remember = findViewById(R.id.rememberBox);

        logIn = findViewById(R.id.logIn1);
        logIn.setOnClickListener(this);

        forgetPassword = findViewById(R.id.forgetPassword);
        forgetPassword.setOnClickListener(this);
        register = findViewById(R.id.register);
        register.setOnClickListener(this);

        SharedPreferences preferences = getSharedPreferences("checkbox",MODE_PRIVATE);
        String checkbox = preferences.getString("remember","");
        if(checkbox.equals("true")){
            Intent intent = new Intent(MainActivity.this,Homepage.class);
            startActivity(intent);
//            userLogin();

        }else if(checkbox.equals("false")){
            Toast.makeText(this, "Please sign in", Toast.LENGTH_SHORT).show();
        }

        remember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isChecked()){

                    SharedPreferences preferences = getSharedPreferences("checkbox",MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember","true");
                    editor.apply();
                    Toast.makeText(MainActivity.this, "Checked", Toast.LENGTH_SHORT).show();

                }else if(!buttonView.isChecked()){

                    SharedPreferences preferences = getSharedPreferences("checkbox",MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("remember","false");
                    editor.apply();
                    Toast.makeText(MainActivity.this, "Unchecked", Toast.LENGTH_SHORT).show();
                }

            }
        });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Token TAG", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        Log.d("Token TAG", token);

                        FirebaseDatabase.getInstance().getReference("Device Token")
                                .child("token")
                                .setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(MainActivity.this, "Saved the code", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(MainActivity.this, "No code saved", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.logIn1:
                userLogin();
                break;
            case R.id.forgetPassword:
                startActivity(new Intent(this,Forget_Password.class));
                break;
            case R.id.register:
                startActivity(new Intent(this,Register.class));
                break;
        }
    }

    private void userLogin(){
        String emailAddress = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();

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

        mAuth.signInWithEmailAndPassword(emailAddress, userPassword).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    startActivity(new Intent(MainActivity.this, Homepage.class));
                }
                else{
                    Toast.makeText(MainActivity.this,"Email or password is incorrect, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}