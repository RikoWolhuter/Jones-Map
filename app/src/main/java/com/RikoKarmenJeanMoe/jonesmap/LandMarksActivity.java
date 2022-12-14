package com.RikoKarmenJeanMoe.jonesmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.AdapterView;
import android.widget.Toast;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.jonesmap.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class LandMarksActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggleOnOff;
    private NavigationView navigationView;

    private static final String TAG = "landMarksActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private ListView lstvLandmarks;
    private List<String> LandmarksList;

    private FirebaseUser user;
    private String userID;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private DatabaseReference registerUsers = database.getReference("JonesMap");
    private FirebaseAuth mAuth;

    Intent intentItem;

    private ImageView imageView2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_land_marks);



        intentItem = new Intent(this, MapActivity.class);

        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.nav_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerLayout = findViewById(R.id.drawer_layout);
        toggleOnOff = new ActionBarDrawerToggle(this, drawerLayout,toolbar, R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggleOnOff);
        toggleOnOff.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        imageView2 = findViewById(R.id.imageView2);
        imageView2.setOnClickListener(this::onClick);



        LandmarksList = new ArrayList<>();
        lstvLandmarks = (ListView) findViewById(R.id.LandmarksList);


        final ArrayAdapter<String> landmarkAdapter = new ArrayAdapter<String>(LandMarksActivity.this, R.layout.custom_list,R.id.text,LandmarksList);
        lstvLandmarks.setAdapter(landmarkAdapter);


        FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("StringLandmarks").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        String value = snapshot.getValue(String.class);
                        LandmarksList.add(value);
                        landmarkAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        landmarkAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        lstvLandmarks.setOnItemClickListener(listClick);
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
                IntentHelper.openIntent(this, ChangeSettings.class);
                break;
            case R.id.nav_profile:
                IntentHelper.openIntent(this, ProfileActivity.class);
                break;

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        //returning true marks the item as selected
        return true;
    }
    public  void onClick(View v)
    {
        switch (v.getId()) {

            case R.id.imageView2:
                IntentHelper.openIntent(this, AddLandMarksActivity.class);
                break;
        }

    }
    @Override
    public void onBackPressed(){

        //if the drawer is open, close it
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            //otherwise, let the super class handle it
            super.onBackPressed();
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG,"isServicesOK: checking google services version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(LandMarksActivity.this);

        if(available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(LandMarksActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private AdapterView.OnItemClickListener listClick = new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView parent, View view, int i, long l) {
            String itemValue  = ((String) lstvLandmarks.getItemAtPosition(i)).trim();

            intentItem.putExtra("Selected Landmark", itemValue);

            Intent intent = new Intent(LandMarksActivity.this,MapActivity.class);
            startActivity(intentItem);

        }
    };


}