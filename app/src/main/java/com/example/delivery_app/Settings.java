package com.example.delivery_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

public class Settings extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    RadioGroup radioGroup;
    Button clearPast;
    Button clearExpected;
    Switch notiSwitch;
    RadioButton light;
    RadioButton dark;
    RadioButton blue;
    RadioButton rose;
    ImageView settingsImage;
    Toolbar tb;
    TextView tv;
    // Database
    private FirebaseAuth mAuth;

    // Navigation
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        notiSwitch = findViewById(R.id.notiSwitch);
        radioGroup = findViewById(R.id.radioGroup);
        clearPast = findViewById(R.id.clearPast);
        clearExpected = findViewById(R.id.clearExpected);
        light = findViewById(R.id.radio_one);
        dark = findViewById(R.id.radio_two);
        blue = findViewById(R.id.radio_three);
        rose = findViewById(R.id.radio_four);
        tv = findViewById(R.id.textView5);
        tb = findViewById(R.id.settingsToolbar);
        settingsImage = findViewById(R.id.settingsImage);
        SharedPreferences sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE);

        /** Navigation Drawer */
        // Navigation Drawer
        setToolbar();
        navigationView = (NavigationView) findViewById(R.id.navigation_menu);
        navigationView.setNavigationItemSelectedListener(this);

        // my_child_toolbar is defined in the layout file
//        Toolbar setToolBar = (Toolbar) findViewById(R.id.settingsToolbar);
//        setSupportActionBar(setToolBar);
//        getSupportActionBar().setTitle("Settings");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enables Up Button

        /** Notification Enable/Disable */
        notiSwitch.setChecked(sharedPref.getBoolean("switch",false));

        notiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
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
                    Toast.makeText(Settings.this, "Notifications enabled.", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                    editor.putBoolean("switch", true);
                    editor.commit();

                }
                else{
                    FirebaseMessaging.getInstance().deleteToken();
                    Toast.makeText(Settings.this, "Notifications disabled.", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
                    editor.putBoolean("switch", false);
                    editor.commit();
                }
            }
        });

        int radiooo = sharedPref.getInt("measurement",R.id.radio_one);
        radioGroup.check(radiooo);


        if(radiooo == R.id.radio_one){
            Drawable unwrappedDrawable = AppCompatResources.getDrawable(Settings.this, R.drawable.border);
            Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
            DrawableCompat.setTint(wrappedDrawable, Color.BLACK);

        }else
            if (radiooo == R.id.radio_two){
                setBlackSettingsUI();
        }else
            if (radiooo == R.id.radio_three) {
                setBlueSettingsUI();
        }else
            if (radiooo == R.id.radio_four){
                setPinkSettingsUI();
        }

        /** Clear Past Deliveries Functionality */
        clearPast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog dialog = new AlertDialog.Builder(Settings.this)
                        .setTitle("Clear Past Deliveries")
                        .setMessage("Are you sure you want to clear past deliveries? (This can't be undone)")
                        .setPositiveButton("Yes", null)
                        .setNegativeButton("Cancel", null)
                        .show();

                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference receivedReference = FirebaseDatabase.getInstance().getReference("Deliveries").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Received");
                        receivedReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Settings.this, "Delivery has been deleted successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Settings.this, "Fail to delete delivery, try again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        Toast.makeText(Settings.this,"Past deliveries have been cleared.",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }});
            }
        });

        /** Clear Expected Deliveries Functionality */
        clearExpected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog dialog = new AlertDialog.Builder(Settings.this)
                        .setTitle("Clear Expected Deliveries")
                        .setMessage("Are you sure you want to clear expected deliveries? (This can't be undone)")
                        .setPositiveButton("Yes", null)
                        .setNegativeButton("Cancel", null)
                        .show();

                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Deliveries").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Processing");
                        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Settings.this, "Delivery has been deleted successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Settings.this, "Fail to delete delivery, try again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        Toast.makeText(Settings.this,"Expected deliveries have been cleared.",Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }});
            }
        });
    }

    // For Navigation Drawer
    private void setToolbar() {
        drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.app_name,R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    // Navigation Drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.homePage:
                startActivity(new Intent(Settings.this, Homepage.class));
                break;

            case R.id.nav_settings:
                startActivity(new Intent(Settings.this, Settings.class));
                break;

            case R.id.nav_scheduled_delivery2:
                startActivity(new Intent(Settings.this, Scheduled_Delivery.class));
                break;

            case R.id.nav_help:
                help(0);
                break;

            case R.id.nav_logout:
                mAuth.signOut();
                startActivity(new Intent(Settings.this, MainActivity.class));
                break;
        }
        return false;
    }

    /** Drop Down Menu for Notification Type */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        int selected_item = parent.getSelectedItemPosition();

        SharedPreferences sharedPref = getSharedPreferences("Settings",0);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putInt("spinner_item", selected_item);
        prefEditor.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void checkButton (View v) {
        int radioId = radioGroup.getCheckedRadioButtonId();

        RadioButton radioButton = findViewById(radioId);

        if(radioId == R.id.radio_one){
            Intent activity2Intent = new Intent(getApplicationContext(), Settings.class);
            startActivity(activity2Intent);
            Toast.makeText(this, "Light mode", Toast.LENGTH_SHORT).show();
        }else if (radioId == R.id.radio_two){
            Intent activity2Intent = new Intent(getApplicationContext(), Settings.class);
            startActivity(activity2Intent);
            Toast.makeText(this, "Dark mode", Toast.LENGTH_SHORT).show();
        }else if (radioId == R.id.radio_three){
            Intent activity2Intent = new Intent(getApplicationContext(), Settings.class);
            startActivity(activity2Intent);
            Toast.makeText(this, "Blue mode", Toast.LENGTH_SHORT).show();
        } else if (radioId == R.id.radio_four){
            Intent activity2Intent = new Intent(getApplicationContext(), Settings.class);
            startActivity(activity2Intent);
            Toast.makeText(this, "Rose mode", Toast.LENGTH_SHORT).show();
        }

        SharedPreferences sharedPref = getSharedPreferences("Settings",0);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putInt("measurement", radioGroup.getCheckedRadioButtonId());
        prefEditor.commit();
    }

    /** Help Fragment from Navigation Drawer */
    private void help (int counter){

        String string[] = {"click the add button in the bottom right corner to add a delivery",
                "enter all the information and click the OK to add the delivery",
                "click on the delivery delivery to edit, delete, or receive the delivery",
                "click on the save to save the modification, click the delete to delete the delivery, click the receive to receive the delivery (after receiving, the delivery will be added to history)",
                "any question or want to sent a feedback to development team? send an email to delivery.app.390@gmail.com"};
        int id[] = {R.drawable.step_one,R.drawable.step_two,R.drawable.step_th,R.drawable.step_four};

        AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);

        LayoutInflater inflater = Settings.this.getLayoutInflater();
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
    public void setBlackSettingsUI(){            tb.setBackgroundColor(Color.GRAY);

        notiSwitch.setTextColor(Color.WHITE);
        tb.setTitleTextColor(Color.WHITE);
        tb.setSubtitleTextColor(Color.WHITE);
        clearPast.setTextColor(Color.WHITE);
        clearExpected.setTextColor(Color.WHITE);
        light.setTextColor(Color.WHITE);
        dark.setTextColor(Color.WHITE);
        blue.setTextColor(Color.WHITE);
        rose.setTextColor(Color.WHITE);
        tv.setTextColor(Color.WHITE);
        settingsImage.setColorFilter(Color.WHITE);

        Drawable unwrappedDrawable = AppCompatResources.getDrawable(Settings.this, R.drawable.border);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Color.WHITE);

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

        DrawerLayout rl = (DrawerLayout)findViewById(R.id.drawerLayout);
        rl.setBackgroundColor(Color.BLACK);}
    public void setBlueSettingsUI(){            tb.setBackgroundColor(Color.parseColor("#82C0CC"));

        notiSwitch.setTextColor(Color.WHITE);
        tb.setTitleTextColor(Color.WHITE);
        tb.setSubtitleTextColor(Color.WHITE);
        clearPast.setTextColor(Color.WHITE);
        clearExpected.setTextColor(Color.WHITE);
        light.setTextColor(Color.WHITE);
        dark.setTextColor(Color.WHITE);
        blue.setTextColor(Color.WHITE);
        rose.setTextColor(Color.WHITE);
        tv.setTextColor(Color.WHITE);
        clearPast.setBackgroundColor(Color.parseColor("#FFA62B"));
        clearExpected.setBackgroundColor(Color.parseColor("#FFA62B"));
        settingsImage.setColorFilter(Color.parseColor("#FFA62B"));

        Drawable unwrappedDrawable = AppCompatResources.getDrawable(Settings.this, R.drawable.border);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Color.WHITE);

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

        DrawerLayout rl = (DrawerLayout)findViewById(R.id.drawerLayout);
        rl.setBackgroundColor(Color.parseColor("#16697A"));}
    public void setPinkSettingsUI(){
        tb.setBackgroundColor(Color.parseColor("#DBABBE"));

        notiSwitch.setTextColor(Color.parseColor("#f8f8ff"));
        tb.setTitleTextColor(Color.parseColor("#f8f8ff"));
        tb.setSubtitleTextColor(Color.parseColor("#f8f8ff"));
        clearPast.setTextColor(Color.parseColor("#f8f8ff"));
        clearExpected.setTextColor(Color.parseColor("#f8f8ff"));
        light.setTextColor(Color.parseColor("#f8f8ff"));
        dark.setTextColor(Color.parseColor("#f8f8ff"));
        blue.setTextColor(Color.parseColor("#f8f8ff"));
        rose.setTextColor(Color.parseColor("#f8f8ff"));
        tv.setTextColor(Color.parseColor("#f8f8ff"));
        clearPast.setBackgroundColor(Color.parseColor("#EDBBB4"));
        clearExpected.setBackgroundColor(Color.parseColor("#EDBBB4"));
        settingsImage.setColorFilter(Color.parseColor("#EDBBB4"));

        Drawable unwrappedDrawable = AppCompatResources.getDrawable(Settings.this, R.drawable.border);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Color.GRAY);

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

        DrawerLayout rl = (DrawerLayout)findViewById(R.id.drawerLayout);
        rl.setBackgroundColor(Color.parseColor("#EDD2E0"));
    }
}