package com.example.jonesmap;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChangeSettings extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggleOnOff;
    private NavigationView navigationView;

    private static final String TAG = "landMarksActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    List<String> placeTypeList;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_settings);
        placeTypeList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference().child("Places");

        //Measurements drop down
        Spinner spinnerMeasurements = findViewById(R.id.preference_measurement);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Measurements,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerMeasurements.setAdapter(adapter);
        spinnerMeasurements.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        // Metric
                        break;
                    case 1:
                        // Imperial
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    //Landmark dropdown
        Spinner spinnerLandmark = findViewById(R.id.preference_landmark);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.Landmark,
                android.R.layout.simple_spinner_item);

        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerLandmark.setAdapter(adapter2);
        spinnerLandmark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        // Restaurant
                        uploadPlaces("restaurant");

                        break;
                    case 1:
                        // Nature Reserve
                        uploadPlaces("nature_reserve");
                        break;
                    case 2:
                        // Beach
                        uploadPlaces("beach");
                        break;
                    case 3:
                        // School
                        uploadPlaces("school");
                        break;
                    case 4:
                        // Hospital
                        uploadPlaces("hospital");
                        break;
                    case 5:
                        // Shopping Mall
                        uploadPlaces("shopping_mall");
                        break;
                    case 6:
                        // Shopping Store
                        uploadPlaces("shopping_store");
                        break;
                    case 7:
                        // Accommodation
                        uploadPlaces("accommodation");
                        break;
                    case 8:
                        // Train Station
                        uploadPlaces("train_station");
                        break;
                    case 9:
                        // Museum
                        uploadPlaces("museum");
                        break;
                    case 10:
                        // Place of religion
                        uploadPlaces("place_of_religion");
                        break;


                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        switch(item.getItemId()){
            case R.id.nav_map:
                if(isServicesOK()) {
                    IntentHelper.openIntent(this, MapActivity.class);
                }
                break;
            case R.id.nav_landmarks:
                IntentHelper.openIntent(this, LandMarksActivity.class);
                break;
            case R.id.nav_settings:
                IntentHelper.openIntent(this, SettingsActivity.class);
                break;
            case R.id.nav_profile:
                IntentHelper.openIntent(this, ProfileActivity.class);
                break;

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        //returning true marks the item as selected
        return true;
    }

    @Override
    public void onBackPressed () {
        //if the drawer is open, close it
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            //otherwise, let the super class handle it
            super.onBackPressed();
        }
    }
    public boolean isServicesOK(){
        Log.d(TAG,"isServicesOK: checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(ChangeSettings.this);

        if(available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(ChangeSettings.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public String uploadPlaces(String data)
    {
        placeTypeList.add(data);


        reference.setValue(placeTypeList)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(getApplicationContext(),"Place added to DB successfully", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"Failed add place to db", Toast.LENGTH_LONG).show();

                        }
                    }
                });

        return data;
    }
}
