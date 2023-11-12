package com.example.delivery_app;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.collection.LLRBNode;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;

public class Homepage extends AppCompatActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    // Database
    private FirebaseAuth mAuth;

    private DatabaseReference reference;

    private TextView backChange,box,boxline,box2,boxline2;

    private Button addButton;
    private DatePickerDialog.OnDateSetListener listener;

    // Navigation
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    protected SharedPreferenceHelper sharedPreferenceHelper;
    TextView tv1,tv2,tv3,tv4,tv5,tv6,tv7,tv8,tv9;
    Toolbar tb;
    Button lock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        sharedPreferenceHelper = new SharedPreferenceHelper(Homepage.this);

        tv1 = findViewById(R.id.youHave);
        tv2 = findViewById(R.id.greetingTime);
        tv3 = findViewById(R.id.myNickname);
        tv4 = findViewById(R.id.textView4);
        tv5 = findViewById(R.id.textView7);
        tv6 = findViewById(R.id.textView5);
        tv7 = findViewById(R.id.nextDeliveryDate);
        tv8 = findViewById(R.id.nextDeliveryText);
        tv9 = findViewById(R.id.nextDeliveryDay);
        tb = findViewById(R.id.toolbar);
        // Init
        backChange = findViewById(R.id.textView3);
        backChange.setText(String.valueOf(sharedPreferenceHelper.getCounter()));

        mAuth = FirebaseAuth.getInstance();

        reference = FirebaseDatabase.getInstance().getReference("Deliveries").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Processing");

        // Navigation Drawer
        setToolbar();
        navigationView = (NavigationView) findViewById(R.id.navigation_menu);
        navigationView.setNavigationItemSelectedListener(this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Notification", "Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Sensor
        DatabaseReference stateRef = FirebaseDatabase.getInstance().getReference("PhotoelectricData");
        DatabaseReference loadWeight = FirebaseDatabase.getInstance().getReference("LoadCellData");

        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(this);
        lock = findViewById(R.id.component_lock);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("LockData");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                String data = dataSnapshot.child("boolean").getValue().toString();
                if(data == "false"){
                    lock.setText("Lock");}
                if(data == "true"){
                    lock.setText("Unlock");}
                lock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(data == "false"){
                            Toast.makeText(Homepage.this, "The box has been locked.", Toast.LENGTH_SHORT).show();
                            reference.child("boolean").setValue(true);
                            lock.setText("Unlock");}
                        if(data == "true"){
                            Toast.makeText(Homepage.this, "The box has been unlocked.", Toast.LENGTH_SHORT).show();
                            reference.child("boolean").setValue(false);
                            lock.setText("lock");}
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });


        box = findViewById(R.id.boxPic);
        boxline = findViewById(R.id.boxLine);
        box2 = findViewById(R.id.boxPic2);
        boxline2 = findViewById(R.id.boxLine2);
        lock = findViewById(R.id.component_lock);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                String data = dataSnapshot.child("boolean").getValue().toString();
                if(data == "false"){
                    lock.setText("Lock");}
                if(data == "true"){
                    lock.setText("Unlock");}
                lock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(data == "false"){
                            Toast.makeText(Homepage.this, "The box has been locked.", Toast.LENGTH_SHORT).show();
                            reference.child("boolean").setValue(true);
                            lock.setText("Unlock");}
                        if(data == "true"){
                            Toast.makeText(Homepage.this, "The box has been unlocked.", Toast.LENGTH_SHORT).show();
                            reference.child("boolean").setValue(false);
                            lock.setText("lock");}
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        DatabaseReference ParcelReference = FirebaseDatabase.getInstance().getReference("ParcelCount");
        ParcelReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                String data = dataSnapshot.child("float").getValue().toString();
                backChange.setText(data);
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });

        DatabaseReference PhotoelectricReference = FirebaseDatabase.getInstance().getReference("PhotoelectricData");
        PhotoelectricReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                String data = dataSnapshot.child("boolean").getValue().toString();
                if(data == "false"){
                    box.getBackground().setTint(Color.parseColor("#D10000"));}
                else if(data == "true"){
                    box.getBackground().setTint(Color.parseColor("#00D100"));
                }
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
            }
        });





        Calendar rightNow = Calendar.getInstance();
        int hourNow = rightNow.get(Calendar.HOUR_OF_DAY);
        if(hourNow > 5 && hourNow < 12) {
            tv2.setText("Mornin' ");
        }else if(hourNow > 12 && hourNow < 17) {
            tv2.setText("Afternoon ");
        }else if(hourNow > 17 || hourNow < 4 ) {
            tv2.setText("Evenin' ");
        }


        SharedPreferences sharedPreff = getSharedPreferences("name", Context.MODE_PRIVATE);
        String pName = sharedPreff.getString("name","User");

        tv3.setText(pName);


        SharedPreferences sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        int radiooo = sharedPref.getInt("measurement",R.id.radio_one);

        if(radiooo == R.id.radio_one){
            tv4.setTextColor(Color.WHITE);
            tv5.setTextColor(Color.WHITE);
            backChange.setTextColor(Color.WHITE);
            tv6.setTextColor(Color.WHITE);
            box2.getBackground().setTint(Color.WHITE);
        }else
            if (radiooo == R.id.radio_two){
            setBlackHomepageUI();
        } else
            if (radiooo == R.id.radio_three){
                setBlueHomepageUI();
        }else
            if (radiooo == R.id.radio_four){
            setPinkHomepageUI();
        }

        if(sharedPref.getBoolean("switch",false)) {
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
                                    if (task.isSuccessful()) {
                                    } else {
                                    }
                                }
                            });
                        }
                    });
        }
        SharedPreferences settings = getSharedPreferences("dialogPref", MODE_PRIVATE);
        boolean dialogShown = settings.getBoolean("dialogShown", false);

        if (!dialogShown) {
            final AlertDialog dialog = new AlertDialog.Builder(Homepage.this)
                    .setTitle("Notifications")
                    .setMessage("Do you want to enable notifications?")
                    .setPositiveButton("Yes", null)
                    .setNegativeButton("No", null)
                    .show();

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if (!task.isSuccessful()) {
                                        Log.w("Token TAG", "Fetching FCM registration token failed", task.getException());
                                        return;
                                    }
            }
        };
        SharedPreferences sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        if(sharedPref.getBoolean("switch",true)){
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

                            FirebaseDatabase.getInstance().getReference("test")
                                    .child("test")
                                    .setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Homepage.this, "Saved the code", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Homepage.this, "No code saved", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
        }
        // Adding Value Event Listener
        stateRef.addValueEventListener(photoelectricListener);
                                    /*
                                    // Get new FCM registration token
                                    String token = task.getResult();

                                    Log.d("Token TAG", token);

                                    FirebaseDatabase.getInstance().getReference("test")
                                            .child("test")
                                            .setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                            } else {
                                            }
                                        }
                                    });
                                }
                            });
                    SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                    editor.putBoolean("switch", true);
                    editor.commit();
                    Toast.makeText(Homepage.this,"Notifications have been enabled.",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }});
            SharedPreferences.Editor editor1 = settings.edit();
            editor1.putBoolean("dialogShown", true);
            editor1.commit();
        }*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addButton:
                dialog(this);
        }
    }



    private void setToolbar() {
        drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Homepage");
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.app_name,R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_scheduled_delivery2:
                startActivity(new Intent(Homepage.this, Scheduled_Delivery.class));
                break;

            case R.id.nav_settings:
                startActivity(new Intent(Homepage.this, Settings.class));
                break;

            case R.id.nav_help:
                help(0);
                break;

            case R.id.nav_logout:
                mAuth.signOut();

                SharedPreferences preferences = getSharedPreferences("checkbox",MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("remember","false");
                editor.apply();

                startActivity(new Intent(Homepage.this, MainActivity.class));
                break;
        }
        return false;
    }

    private void dialog (Context context){

        Context c=context;
        AlertDialog.Builder builder = new AlertDialog.Builder(Homepage.this);

        LayoutInflater inflater = Homepage.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog, null);

        TextView datePicker = view.findViewById(R.id.datePicker);
        EditText itemName = view.findViewById(R.id.itemName);

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(Homepage.this, android.R.style.Theme_Holo_Dialog_MinWidth,listener,year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Date deliveryDate = new Date(year-1900, month, dayOfMonth);
                Date today = new Date();
                if(today.after(deliveryDate)){
                    Toast.makeText(Homepage.this, "You are picking a past date", Toast.LENGTH_SHORT).show();
                }
                month = month + 1;
                String date = year + "-" + month + "-" + dayOfMonth;
                datePicker.setText(date);
            }
        };

        builder.setView(view)
                .setTitle("Add new delivery")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // if cancel is clicked, do nothing
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (datePicker.getText().toString().length() == 0 || itemName.getText().toString().length() == 0) {
                            Toast.makeText(Homepage.this, "Some of the input are empty, please fill in all information", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Delivery delivery = new Delivery(itemName.getText().toString(), datePicker.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getUid());
                            reference.push().setValue(delivery).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Homepage.this, "Delivery has been added successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Homepage.this, "Fail to add delivery, try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                })
                .show();

    }

    /*
    private void displayAll(){
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                String[] name = new String[(int)dataSnapshot.getChildrenCount()];
                String[] date = new String[(int)dataSnapshot.getChildrenCount()];
                String[] allName = new String[(int)dataSnapshot.getChildrenCount()];
                int index = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Delivery delivery = snapshot.getValue(Delivery.class);
                    name[index] = "Item Name: "+ delivery.itemName;
                    allName[index] = snapshot.getKey();
                    date[index] = "Arrival date: " + delivery.date;
                    index++;
                }

                adapter = new MyAdapter(Homepage.this, name, date);
                listView.setAdapter(adapter);

                setTracking(allName);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }*/


    /*
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String clickedID = allItemName[position];
        Intent goToProfileActivity = new Intent(Homepage.this, EditDelivery.class);
        goToProfileActivity.putExtra("clickedID", clickedID);
        startActivity(goToProfileActivity);
    }

    class MyAdapter extends ArrayAdapter<String> {
        Context context;
        String[] allProfiles, allDate;
        MyAdapter(Context context, String[] allProfiles, String[] allDate) {
            super(context, R.layout.row, allProfiles);
            this.context = context;
            this.allProfiles = allProfiles;
            this.allDate = allDate;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.row,parent,false);

            TextView information = row.findViewById(R.id.title);
            information.setText(allProfiles[position]);
            TextView date = row.findViewById(R.id.date);
            date.setText(allDate[position]);
            return row;
        }
    }*/

    /*
    private void setTracking(String[] ItemN){
        this.allItemName = ItemN;
    }*/



    private void help (int counter){

        String string[] = {"click the add button in the bottom right corner to add a delivery",
                "enter all the information and click the OK to add the delivery",
                "click on the delivery delivery to edit, delete, or receive the delivery",
                "click on the save to save the modification, click the delete to delete the delivery, click the receive to receive the delivery (after receiving, the delivery will be added to history)",
                "any question or want to sent a feedback to development team? send an email to delivery.app.390@gmail.com"};
        int id[] = {R.drawable.step_one,R.drawable.step_two,R.drawable.step_th,R.drawable.step_four};

        AlertDialog.Builder builder = new AlertDialog.Builder(Homepage.this);

        LayoutInflater inflater = Homepage.this.getLayoutInflater();
        View view = inflater.inflate(R.layout.help_dialog, null);

        ImageView helpImage = view.findViewById(R.id.helpImage);
        TextView helpText = view.findViewById(R.id.helpText);
        helpText.setText(string[counter]);
        if(counter<4) {
            helpImage.setImageResource(id[counter]);
        }


        builder.setView(view)
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // if cancel is clicked, do nothing
                    }
                })
                .setPositiveButton(counter < 4?"NEXT":"Finish", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(counter<4) {
                            help(counter + 1);
                        }
                    }
                })
                .show();

    }

    public void setBlackHomepageUI(){tb.setBackgroundColor(Color.GRAY);

        backChange.setTextColor(Color.WHITE);
        tb.setTitleTextColor(Color.WHITE);
        tb.setSubtitleTextColor(Color.WHITE);
        tv1.setTextColor(Color.WHITE);
        tv2.setTextColor(Color.WHITE);
        tv3.setTextColor(Color.WHITE);
        tv4.setTextColor(Color.WHITE);
        tv5.setTextColor(Color.WHITE);
        tv6.setTextColor(Color.WHITE);
        tv7.setTextColor(Color.BLACK);
        tv8.setTextColor(Color.BLACK);
        tv9.setTextColor(Color.RED);
        boxline.getBackground().setTint(Color.GRAY);
        box2.getBackground().setTint(Color.WHITE);
        boxline2.getBackground().setTint(Color.GRAY);

        int[][] state = new int[][] {
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] {android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_pressed}  // pressed

        };

        int[] color = new int[] {
                Color.WHITE,
                Color.WHITE,
                Color.WHITE,
                Color.WHITE
        };
        int[][] states = new int[][] {
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] {android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_pressed}  // pressed

        };

        int[] colors = new int[] {
                Color.WHITE,
                Color.WHITE,
                Color.WHITE,
                Color.WHITE
        };
        ColorStateList csl = new ColorStateList(state, color);
        ColorStateList csl2 = new ColorStateList(states, colors);

        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(csl2);
        navigationView.setBackgroundColor(Color.DKGRAY);

        ScrollView rl = (ScrollView) findViewById(R.id.your_layout_id);
        rl.setBackgroundColor(Color.BLACK);}
    public void setBlueHomepageUI(){
        tb.setBackgroundColor(Color.parseColor("#82C0CC"));

        backChange.setTextColor(Color.WHITE);
        tb.setTitleTextColor(Color.WHITE);
        tb.setSubtitleTextColor(Color.WHITE);
        tv1.setTextColor(Color.WHITE);
        tv2.setTextColor(Color.WHITE);
        tv3.setTextColor(Color.WHITE);
        tv4.setTextColor(Color.WHITE);
        tv5.setTextColor(Color.WHITE);
        tv6.setTextColor(Color.WHITE);
        tv7.setTextColor(Color.BLACK);
        tv8.setTextColor(Color.BLACK);
        tv9.setTextColor(Color.RED);
        lock.setBackgroundColor(Color.parseColor("#FFA62B"));
        addButton.setBackgroundColor(Color.parseColor("#FFA62B"));
        box2.getBackground().setTint(Color.WHITE);
        boxline2.getBackground().setTint(Color.BLACK);

        int[][] state = new int[][] {
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] {android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_pressed}  // pressed

        };

        int[] color = new int[] {
                Color.WHITE,
                Color.WHITE,
                Color.WHITE,
                Color.WHITE
        };
        int[][] states = new int[][] {
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] {android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_pressed}  // pressed

        };

        int[] colors = new int[] {
                Color.WHITE,
                Color.WHITE,
                Color.WHITE,
                Color.WHITE
        };
        ColorStateList csl = new ColorStateList(state, color);
        ColorStateList csl2 = new ColorStateList(states, colors);

        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(csl2);
        navigationView.setBackgroundColor(Color.parseColor("#489FB5"));

        ScrollView rl = (ScrollView) findViewById(R.id.your_layout_id);
        rl.setBackgroundColor(Color.parseColor("#16697A"));}
    public void setPinkHomepageUI(){
        tb.setBackgroundColor(Color.parseColor("#DBABBE"));

        backChange.setTextColor(Color.parseColor("#f8f8ff"));
        tb.setTitleTextColor(Color.parseColor("#f8f8ff"));
        tb.setSubtitleTextColor(Color.parseColor("#f8f8ff"));
        tv1.setTextColor(Color.parseColor("#f8f8ff"));
        tv2.setTextColor(Color.parseColor("#f8f8ff"));
        tv3.setTextColor(Color.parseColor("#f8f8ff"));
        tv4.setTextColor(Color.parseColor("#f8f8ff"));
        tv5.setTextColor(Color.parseColor("#f8f8ff"));
        tv6.setTextColor(Color.parseColor("#f8f8ff"));
        tv7.setTextColor(Color.BLACK);
        tv8.setTextColor(Color.BLACK);
        tv9.setTextColor(Color.RED);
        lock.setTextColor(Color.parseColor("#f8f8ff"));
        lock.setBackgroundColor(Color.parseColor("#EDBBB4"));
        addButton.setTextColor(Color.parseColor("#f8f8ff"));
        addButton.setBackgroundColor(Color.parseColor("#EDBBB4"));
        box2.getBackground().setTint(Color.WHITE);
        boxline2.getBackground().setTint(Color.BLACK);

        int[][] state = new int[][] {
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] {android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_pressed}  // pressed

        };

        int[] color = new int[] {
                Color.parseColor("#f8f8ff"),
                Color.parseColor("#f8f8ff"),
                Color.parseColor("#f8f8ff"),
                Color.parseColor("#f8f8ff")
        };
        int[][] states = new int[][] {
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] {android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_pressed}  // pressed

        };

        int[] colors = new int[] {
                Color.parseColor("#f8f8ff"),
                Color.parseColor("#f8f8ff"),
                Color.parseColor("#f8f8ff"),
                Color.parseColor("#f8f8ff")
        };
        ColorStateList csl = new ColorStateList(state, color);
        ColorStateList csl2 = new ColorStateList(states, colors);

        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(csl2);
        navigationView.setBackgroundColor(Color.parseColor("#DBABBE"));

        ScrollView rl = (ScrollView) findViewById(R.id.your_layout_id);
        rl.setBackgroundColor(Color.parseColor("#EDD2E0"));}
}



