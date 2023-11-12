package com.example.delivery_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditDelivery extends AppCompatActivity
implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private DatabaseReference receivedReference;

    private EditText itemName;

    private TextView datePicker;
    private Button save, delete, receive;
    private String clickedID;

    private DatePickerDialog.OnDateSetListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_delivery);

        clickedID = getIntent().getStringExtra("clickedID");

        itemName = findViewById(R.id.itemName1);
        datePicker = findViewById(R.id.datePicker1);
        datePicker.setOnClickListener(this);

        save = findViewById(R.id.save);
        save.setOnClickListener(this);
        delete = findViewById(R.id.delete);
        delete.setOnClickListener(this);
        receive = findViewById(R.id.receive);
        receive.setOnClickListener(this);

        Toolbar editToolbar =
                (Toolbar) findViewById(R.id.editDeliveryToolbar);
        setSupportActionBar(editToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);

        reference = FirebaseDatabase.getInstance().getReference("Deliveries").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Processing");
        receivedReference = FirebaseDatabase.getInstance().getReference("Deliveries").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Received");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Delivery delivery = snapshot.getValue(Delivery.class);
                    if (clickedID.equals(snapshot.getKey())){
                        itemName.setText(delivery.itemName);
                        datePicker.setText(delivery.date);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        SharedPreferences sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        int radiooo = sharedPref.getInt("measurement",R.id.radio_one);

        if (radiooo == R.id.radio_one) {
        } else if (radiooo == R.id.radio_two) {
            editToolbar.setBackgroundColor(Color.GRAY);
            editToolbar.setSubtitleTextColor(Color.WHITE);
            editToolbar.setTitleTextColor(Color.WHITE);

            datePicker.setTextColor(Color.WHITE);
            itemName.setTextColor(Color.WHITE);

            ConstraintLayout rl = (ConstraintLayout) findViewById(R.id.editLayout);
            rl.setBackgroundColor(Color.BLACK);
        }
        else if (radiooo == R.id.radio_three) {
            editToolbar.setBackgroundColor(Color.parseColor("#82C0CC"));
            editToolbar.setSubtitleTextColor(Color.WHITE);
            editToolbar.setTitleTextColor(Color.WHITE);

            datePicker.setTextColor(Color.WHITE);
            itemName.setTextColor(Color.WHITE);
            save.setTextColor(Color.WHITE);
            receive.setTextColor(Color.WHITE);
            delete.setTextColor(Color.WHITE);
            save.setBackgroundColor(Color.parseColor("#FFA62B"));
            receive.setBackgroundColor(Color.parseColor("#FFA62B"));
            delete.setBackgroundColor(Color.parseColor("#FFA62B"));

            ConstraintLayout rl = (ConstraintLayout) findViewById(R.id.editLayout);
            rl.setBackgroundColor(Color.parseColor("#489FB5"));
        }
        else if (radiooo == R.id.radio_four) {
            editToolbar.setBackgroundColor(Color.parseColor("#DBABBE"));
            editToolbar.setSubtitleTextColor(Color.parseColor("#DBABBE"));
            editToolbar.setTitleTextColor(Color.parseColor("#DBABBE"));

            datePicker.setTextColor(Color.parseColor("#f8f8ff"));
            itemName.setTextColor(Color.parseColor("#f8f8ff"));
            save.setTextColor(Color.parseColor("#f8f8ff"));
            receive.setTextColor(Color.parseColor("#f8f8ff"));
            delete.setTextColor(Color.parseColor("#f8f8ff"));
            save.setBackgroundColor(Color.parseColor("#EDBBB4"));
            receive.setBackgroundColor(Color.parseColor("#EDBBB4"));
            delete.setBackgroundColor(Color.parseColor("#EDBBB4"));

            ConstraintLayout rl = (ConstraintLayout) findViewById(R.id.editLayout);
            rl.setBackgroundColor(Color.parseColor("#EDD2E0"));
        }
    }

    @Override
    public void onClick(View v) {
        Delivery delivery = new Delivery(itemName.getText().toString(), datePicker.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getUid());
        switch(v.getId()){
            case R.id.datePicker1:
                datePicker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar cal = Calendar.getInstance();
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int day = cal.get(Calendar.DAY_OF_MONTH);
                        DatePickerDialog dialog = new DatePickerDialog(EditDelivery.this, android.R.style.Theme_Holo_Dialog_MinWidth,listener,year,month,day);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();
                    }
                });

                listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month + 1;
                        String date = year + "-" + month + "-" + dayOfMonth;
                        datePicker.setText(date);
                    }
                };
                break;

            case R.id.save:
                reference.child(clickedID).removeValue();
                reference.child(clickedID).setValue(delivery).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditDelivery.this, "Delivery has been save successfully", Toast.LENGTH_SHORT).show();
                            Intent goToScheduled_Delivery = new Intent( EditDelivery.this, Scheduled_Delivery.class);
                            startActivity(goToScheduled_Delivery);
                        } else {
                            Toast.makeText(EditDelivery.this, "Fail to save delivery, try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.receive:
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd @ hh:mm:ss a", Locale.getDefault() );
                String today = df.format(new Date());
                Delivery deliveryy = new Delivery(itemName.getText().toString(), datePicker.getText().toString()+" \nReceived on: " + today   , FirebaseAuth.getInstance().getCurrentUser().getUid());
                receivedReference.child(clickedID).setValue(deliveryy);
                reference.child(clickedID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditDelivery.this, "Delivery has been received successfully", Toast.LENGTH_SHORT).show();
                            Intent goToScheduled_Delivery = new Intent( EditDelivery.this, Scheduled_Delivery.class);
                            startActivity(goToScheduled_Delivery);
                        } else {
                            Toast.makeText(EditDelivery.this, "Fail to receive delivery, try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.delete:
                reference.child(clickedID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditDelivery.this, "Delivery has been deleted successfully", Toast.LENGTH_SHORT).show();
                            Intent goToScheduled_Delivery = new Intent( EditDelivery.this, Scheduled_Delivery.class);
                            startActivity(goToScheduled_Delivery);
                        } else {
                            Toast.makeText(EditDelivery.this, "Fail to delete delivery, try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
        }
    }
}