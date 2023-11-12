package com.example.delivery_app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.collection.LLRBNode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Scheduled_Delivery extends AppCompatActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
    // Database
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private DatabaseReference receivedReference;

    private MyAdapter adapter;
    private MyAdapter adapter1;

    private Button addButton;
    private ListView listView;
    private ListView listView1;
    private DatePickerDialog.OnDateSetListener listener;

    private String[] allItemName;
    private String[] allItemName1;

    // Navigation
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    TextView border,border1,tvE,tvP;
    Toolbar tb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduled_delivery2);

        mAuth = FirebaseAuth.getInstance();

        reference = FirebaseDatabase.getInstance().getReference("Deliveries").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Processing");
        receivedReference = FirebaseDatabase.getInstance().getReference("Deliveries").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Received");

        // Navigation Drawer
        setToolbar();
        navigationView = (NavigationView) findViewById(R.id.navigation_menu);
        navigationView.setNavigationItemSelectedListener(this);

        listView = findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        listView1 = findViewById(R.id.listView1);
        listView1.setOnItemLongClickListener(this);

        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        displayAll("");

        tb = findViewById(R.id.toolbar);
        tvE = findViewById(R.id.tvE);
        tvP = findViewById(R.id.tvP);
        border = findViewById(R.id.border);
        border1 = findViewById(R.id.border1);

        SharedPreferences sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        int radiooo = sharedPref.getInt("measurement",R.id.radio_one);

        if(radiooo == R.id.radio_one){
        }else if (radiooo == R.id.radio_two){
            setBlackSdUI();

        }else if (radiooo == R.id.radio_three){
            setBlueSdUI();
        }
        else if (radiooo == R.id.radio_four){
            setPinkSdUI();
        }
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
        getSupportActionBar().setTitle("Scheduled Delivery            Search");
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.app_name,R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_scheduled_delivery2:
                startActivity(new Intent(Scheduled_Delivery.this, Scheduled_Delivery.class));
                break;

            case R.id.nav_settings:
                startActivity(new Intent(Scheduled_Delivery.this, Settings.class));
                break;

            case R.id.nav_help:
                help(0);
                break;

            case R.id.homePage:
                startActivity(new Intent(Scheduled_Delivery.this, Homepage.class));
                break;

            case R.id.nav_logout:
                mAuth.signOut();

                SharedPreferences preferences = getSharedPreferences("checkbox",MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("remember","false");
                editor.apply();
                startActivity(new Intent(Scheduled_Delivery.this, MainActivity.class));
                break;
        }
        return false;
    }


    private void dialog (Context context){

        Context c=context;
        AlertDialog.Builder builder = new AlertDialog.Builder(Scheduled_Delivery.this);

        LayoutInflater inflater = Scheduled_Delivery.this.getLayoutInflater();
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

                DatePickerDialog dialog = new DatePickerDialog(Scheduled_Delivery.this, android.R.style.Theme_Holo_Dialog_MinWidth,listener,year,month,day);
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
                    Toast.makeText(Scheduled_Delivery.this, "You are picking a past date", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(Scheduled_Delivery.this, "Some of the input are empty, please fill in all information", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Delivery delivery = new Delivery(itemName.getText().toString(), datePicker.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getUid());
                            reference.push().setValue(delivery).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Scheduled_Delivery.this, "Delivery has been added successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Scheduled_Delivery.this, "Fail to add delivery, try again", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                })
                .show();

    }


    private void displayAll(String constrain){
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                String[] name = new String[(int)dataSnapshot.getChildrenCount()];
                String[] date = new String[(int)dataSnapshot.getChildrenCount()];
                String[] allItem = new String[(int)dataSnapshot.getChildrenCount()];

                int index = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Delivery delivery = snapshot.getValue(Delivery.class);
                    if(delivery.itemName.toLowerCase().contains(constrain.toLowerCase().trim())) {
                        name[index] = "Item Name: " + delivery.itemName;
                        allItem[index] = snapshot.getKey();
                        date[index] = "Arrival date: " + delivery.date;

                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            Date datee = format.parse(delivery.date);
                            Date dt = new Date();
                            Calendar c = Calendar.getInstance();
                            c.setTime(dt);
                            c.add(Calendar.DATE, -1);
                            dt = c.getTime();
                            if(dt.after(datee)){
                                date[index] = "Arrival date: " + delivery.date + " *Date has passed!";
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        index++;
                    }
                }

                adapter = new MyAdapter(Scheduled_Delivery.this, name, date);

                listView.setAdapter(adapter);

                setTracking(allItem);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }

        });

        receivedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                String[] name1 = new String[(int)dataSnapshot.getChildrenCount()];
                String[] date1 = new String[(int)dataSnapshot.getChildrenCount()];
                String[] allItem1 = new String[(int)dataSnapshot.getChildrenCount()];

                int index = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Delivery delivery = snapshot.getValue(Delivery.class);
                    if(delivery.itemName.toLowerCase().contains(constrain.toLowerCase().trim())) {
                        name1[index] = "Item Name: "+delivery.itemName;
                         allItem1[index] = snapshot.getKey();
                        date1[index] = "Set arrival date: " + delivery.date;
                        index++;
                    }
                }

                adapter1 = new MyAdapter(Scheduled_Delivery.this, name1, date1);

                listView1.setAdapter(adapter1);

                setTracking1(allItem1);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String clickedID = allItemName[position];
        Intent goToProfileActivity = new Intent(Scheduled_Delivery.this, EditDelivery.class);
        goToProfileActivity.putExtra("clickedID", clickedID);
        startActivity(goToProfileActivity);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        new AlertDialog.Builder(Scheduled_Delivery.this)
                .setTitle("Confirmation")
                .setMessage("Do you want to delete this delivery?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        receivedReference.child(allItemName1[position]).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Scheduled_Delivery.this, "Delivery has been deleted successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Scheduled_Delivery.this, "Fail to delete delivery, try again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("No", null)
                .show();
        return true;
    }


    class MyAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> exampleList;
        private ArrayList<String> exampleListFull;
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
            SharedPreferences sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE);
            int radiooo = sharedPref.getInt("measurement",R.id.radio_one);

            if(radiooo == R.id.radio_one){
            }else if (radiooo == R.id.radio_two){
                information.setTextColor(Color.WHITE);
                date.setTextColor(Color.WHITE);
                listView.setBackgroundColor(Color.BLACK);
                listView1.setBackgroundColor(Color.BLACK);

                ScrollView rl = (ScrollView) findViewById(R.id.scrollLayout);
                rl.setBackgroundColor(Color.BLACK);

            }else if (radiooo == R.id.radio_three){
                information.setTextColor(Color.WHITE);
                date.setTextColor(Color.WHITE);
                listView.setBackgroundColor(Color.parseColor("#16697A"));
                listView1.setBackgroundColor(Color.parseColor("#16697A"));

                ScrollView rl = (ScrollView) findViewById(R.id.scrollLayout);
                rl.setBackgroundColor(Color.parseColor("#16697A"));
            }
            else if (radiooo == R.id.radio_four){
                information.setTextColor(Color.parseColor("#f8f8ff"));
                date.setTextColor(Color.parseColor("#f8f8ff"));
                listView.setBackgroundColor(Color.parseColor("#EDD2E0"));
                listView1.setBackgroundColor(Color.parseColor("#EDD2E0"));

                ScrollView rl = (ScrollView) findViewById(R.id.scrollLayout);
                rl.setBackgroundColor(Color.parseColor("#EDD2E0"));
            }
            return row;
        }

        public Filter getFilter(){
            return researchFilter;
        }

        private Filter researchFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                displayAll(constraint.toString());
            }
        };
    }



    private void setTracking(String[] item){
        this.allItemName = item;
        //Toast.makeText(Scheduled_Delivery.this, allTrackingNumber[0],Toast.LENGTH_SHORT).show();
    }

    private void setTracking1(String[] item){
        this.allItemName1 = item;
        //Toast.makeText(Scheduled_Delivery.this, allTrackingNumber[0],Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.schedule_delivery,menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search Here");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                adapter1.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    private void help (int counter){

        String string[] = {"click the add button in the bottom right corner to add a delivery",
                "enter all the information and click the OK to add the delivery",
                "click on the delivery delivery to edit, delete, or receive the delivery",
                "click on the save to save the modification, click the delete to delete the delivery, click the receive to receive the delivery (after receiving, the delivery will be added to history)",
                "any question or want to sent a feedback to development team? send an email to delivery.app.390@gmail.com"};
        int id[] = {R.drawable.step_one,R.drawable.step_two,R.drawable.step_th,R.drawable.step_four};

        AlertDialog.Builder builder = new AlertDialog.Builder(Scheduled_Delivery.this);

        LayoutInflater inflater = Scheduled_Delivery.this.getLayoutInflater();
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
    public void setBlackSdUI(){            tb.setBackgroundColor(Color.GRAY);
        tb.setTitleTextColor(Color.WHITE);
        tb.setSubtitleTextColor(Color.WHITE);
        tvE.setTextColor(Color.WHITE);
        tvP.setTextColor(Color.WHITE);
        tvE.setBackgroundColor(Color.BLACK);
        tvP.setBackgroundColor(Color.BLACK);
        listView.setBackgroundColor(Color.BLACK);
        listView1.setBackgroundColor(Color.BLACK);

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

        ScrollView rl = (ScrollView) findViewById(R.id.scrollLayout);
        rl.setBackgroundColor(Color.BLACK);}
    public void setBlueSdUI(){            tb.setBackgroundColor(Color.parseColor("#82C0CC"));

        tb.setTitleTextColor(Color.WHITE);
        tb.setSubtitleTextColor(Color.WHITE);
        tvE.setTextColor(Color.WHITE);
        tvP.setTextColor(Color.WHITE);
        tvE.setBackgroundColor(Color.parseColor("#16697A"));
        tvP.setBackgroundColor(Color.parseColor("#16697A"));
        addButton.setBackgroundColor(Color.parseColor("#FFA62B"));
        listView.setBackgroundColor(Color.parseColor("#16697A"));
        listView1.setBackgroundColor(Color.parseColor("#16697A"));

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

        ScrollView rl = (ScrollView) findViewById(R.id.scrollLayout);
        rl.setBackgroundColor(Color.parseColor("#16697A"));}
    public void setPinkSdUI(){            tb.setBackgroundColor(Color.parseColor("#DBABBE"));

        tb.setTitleTextColor(Color.parseColor("#f8f8ff"));
        tb.setSubtitleTextColor(Color.parseColor("#f8f8ff"));
        tvE.setTextColor(Color.parseColor("#f8f8ff"));
        tvP.setTextColor(Color.parseColor("#f8f8ff"));
        tvE.setBackgroundColor(Color.parseColor("#EDD2E0"));
        tvP.setBackgroundColor(Color.parseColor("#EDD2E0"));
        addButton.setTextColor(Color.parseColor("#f8f8ff"));
        addButton.setBackgroundColor(Color.parseColor("#EDBBB4"));
        listView.setBackgroundColor(Color.parseColor("#EDD2E0"));
        listView1.setBackgroundColor(Color.parseColor("#EDD2E0"));

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

        ScrollView rl = (ScrollView) findViewById(R.id.scrollLayout);
        rl.setBackgroundColor(Color.parseColor("#EDD2E0"));}
}