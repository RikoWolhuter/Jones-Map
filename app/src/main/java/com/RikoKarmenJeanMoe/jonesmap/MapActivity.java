package com.RikoKarmenJeanMoe.jonesmap;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.RikoKarmenJeanMoe.jonesmap.models.PlaceInfo;
import com.example.jonesmap.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.GeoApiContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by User on 10/2/2017.
 */

public class MapActivity<ActivityReadDataBinding> extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener {

    private FirebaseUser user;
    private String userID;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private DatabaseReference registerUsers = database.getReference("JonesMap");
    private FirebaseAuth mAuth;

    public String tempLandmark;
    public String searchString;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
//WORKS
    @Override
    public void onMapReady(GoogleMap googleMap) {
        String message = "You are using Jones's Map and its running in the background!";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MapActivity.this)
                .setSmallIcon(R.drawable.maps__1__1)
                .setContentTitle("Jone's Map")
                .setContentText(message)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());

        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }
        //direction();
        googleMap.setOnInfoWindowClickListener(this);
    }


    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));
    private static final int PLACE_PICKER_REQUEST = 1;
    private static String MyPosLat = "";
    private static String MyPosLong = "";
    private static double MyPosLatNum = 0;
    private static double MyPosLongNum = 0;
    String place;
    String measurement;

    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps, mInfo, mPlacePicker;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;
    private Marker mMarker;
    SupportMapFragment supportMapFragment;
    private GeoApiContext mGeoApiContext = null;
    private DatabaseReference reference;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        String str = intent.getStringExtra("Selected Landmark");

        //Toast.makeText(getApplicationContext(),str,Toast.LENGTH_SHORT).show();

        mSearchText = (AutoCompleteTextView) findViewById(R.id.input_search);

        mSearchText.setText(str);
        mGps = (ImageView) findViewById(R.id.ic_gps);
        mInfo = (ImageView) findViewById(R.id.place_info);
        mPlacePicker = (ImageView) findViewById(R.id.place_picker);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();

        mAuth = FirebaseAuth.getInstance();
        Measurement();
/*
        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
        */

       // getDeviceLocation();

        //initialize url
       /* String url = "https://maps.googleapis.com/maps/api/place/nearbysearcg/json" +//url
                "?location=" + MyPosLatNum + "," + MyPosLongNum +//location latitude and longitude
                "&radius=5000" +//nearby radius
                "&types= " + "church" +//place type
                "&sensor =true" +//sensor

                "key=" + getResources().getString(R.string.google_maps_API_key);//google map key

        new PlaceTask().execute(url);*/

        //getCurrentLocation();

    }
    private void init(){
        Log.d(TAG, "init: initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mSearchText.setOnItemClickListener(mAutocompleteClickListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked place info");
                try{
                    if(mMarker.isInfoWindowShown()){
                        mMarker.hideInfoWindow();
                    }else{
                        Log.d(TAG, "onClick: place info: " + mPlace.toString());
                        mMarker.showInfoWindow();
                    }
                }catch(NullPointerException e){
                    Log.e(TAG, "onClick: NullPointerException: "+ e.getMessage() );
                }
            }
        });

        mPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(MapActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.e(TAG, "onClick: GooglePlayServicesRepairableException: " + e.getMessage() );
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.e(TAG, "onClick: GooglePlayServicesNotAvailableException: " + e.getMessage() );
                }
            }
        });

        hideSoftKeyboard();
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }


    }

    private void getCurrentLocation() {
        //Initialize task Location
        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) ;
        {
           // supportMapFragment.getMapAsync(this);
            Task<Location> task = mFusedLocationProviderClient.getLastLocation();

            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location!= null){
                        //get current latitude
                        MyPosLatNum = location.getLatitude();


                        MyPosLongNum = location.getLongitude();

                        //Sync map
                        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(@NonNull GoogleMap googleMap) {
                                //when map is ready
                                mMap =googleMap;

                                //googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                                // Toast.makeText(getApplicationContext(),"latitude"+ MyPosLatNum, Toast.LENGTH_LONG).show();


                                //Zoom current location on map
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(MyPosLatNum,MyPosLongNum),10


                                ));
                            }
                        });
                    }
                }
            });
        }


    }
    private String Measurement() {


        FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("Measurements").exists()) {
                    measurement = snapshot.child("Measurements").getValue(String.class);
                   // Toast.makeText(getApplicationContext(), measurement, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    measurement = "kilometers";
                    //Toast.makeText(getApplicationContext(), measurement, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return measurement;
    }





    private String findPlace()
    {
        reference=  FirebaseDatabase.getInstance().getReference("Users").
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Places");
        reference.child("Places").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if(snapshot.exists())
                        {
                              place = snapshot.getValue(String.class);

                        }
                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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

        reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(),"Place able to read successfully"+ place, Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Failed to read place", Toast.LENGTH_LONG).show();
                }
            }
        });
        return place;
    }

    private String downloadUrl(String string) throws IOException{
        //Initialize url
        URL url= new URL(string);
        //Initialize connection
        HttpURLConnection connection =(HttpURLConnection) url.openConnection();
        //Connect connection
        connection.connect();
        //Initialize inputstream
        InputStream stream= connection.getInputStream();
        //Initialize buffer reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        //Initialize string builder
        StringBuilder builder = new StringBuilder();
        //Initialize string variable
        String line = "";
        //Use while loop
        while ((line = reader.readLine()) != null) {
            //append line
            builder.append(line);
        }
        //get append data
        String data = builder.toString();
        //Close reader
        reader.close();
        //return data
        return data;
    }

    //to show nearby of places



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);

                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, place.getId());
                placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            }
        }
    }

    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My Location");

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void moveCamera(LatLng latLng, float zoom, PlaceInfo placeInfo){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        mMap.clear();

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));
        if(placeInfo != null){
            try{
                String snippet = "Address: " + placeInfo.getAddress() + "\n" +
                        "Phone Number: " + placeInfo.getPhoneNumber() + "\n" +
                        "Website: " + placeInfo.getWebsiteUri() + "\n" +
                        "Price Rating: " + placeInfo.getRating() + "\n"
                        + "\n"+
                        "Determine route to location?" +  "\n";

                MarkerOptions options = new MarkerOptions()
                        .position(latLng).title(placeInfo.getName()).snippet(snippet);

                mMarker = mMap.addMarker(options);

            }catch(NullPointerException e){
                Log.e(TAG, "moveCamera: NullPointerException: " + e.getMessage());
            }
        }else{


            mMap.addMarker(new MarkerOptions().position(latLng));
        }

        hideSoftKeyboard();
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        MyPosLat = String.valueOf(latLng.latitude);
        MyPosLong = String.valueOf(latLng.longitude);
        MyPosLatNum = latLng.latitude;
        MyPosLongNum = latLng.longitude;
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("My Location")){
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();

    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0||requestCode==44) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                    getDeviceLocation();
                }
            }
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    private void direction(Marker marker){


        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json")
                .buildUpon()
                .appendQueryParameter("destination", destination.toString())
                .appendQueryParameter("origin", (MyPosLat+", "+MyPosLong))
                .appendQueryParameter("mode", "driving")
                .appendQueryParameter("key", getString(R.string.google_maps_API_key))
                .appendQueryParameter("duration","minutes")
                .appendQueryParameter("distance",Measurement())
                .toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");
                    if (status.equals("OK")) {
                        JSONArray routes = response.getJSONArray("routes");

                        ArrayList<LatLng> points;
                        PolylineOptions polylineOptions = null;

                        for (int i=0;i<routes.length();i++){
                            points = new ArrayList<>();
                            polylineOptions = new PolylineOptions();
                            JSONArray legs = routes.getJSONObject(i).getJSONArray("legs");

                            for (int j=0;j<legs.length();j++){
                                JSONArray steps = legs.getJSONObject(j).getJSONArray("steps");

                                for (int k=0;k<steps.length();k++){
                                    String polyline = steps.getJSONObject(k).getJSONObject("polyline").getString("points");
                                    List<LatLng> list = decodePoly(polyline);

                                    for (int l=0;l<list.size();l++){
                                        LatLng position = new LatLng((list.get(l)).latitude, (list.get(l)).longitude);
                                        points.add(position);
                                    }
                                }
                            }
                            polylineOptions.addAll(points);
                            polylineOptions.width(10);
                            polylineOptions.color(ContextCompat.getColor(MapActivity.this, R.color.purple_500));
                            polylineOptions.geodesic(true);
                            Toast.makeText(MapActivity.this, "Metric used: " + Measurement(), Toast.LENGTH_SHORT).show();
                        }
                        mMap.addPolyline(polylineOptions);


                        /*
                        mMap.addMarker(new MarkerOptions().position(new LatLng(-6.9249233, 107.6345122)).title("Marker 1"));
                        mMap.addMarker(new MarkerOptions().position(new LatLng(-6.9218571, 107.6048254)).title("Marker 1"));
*/

                        LatLngBounds bounds = new LatLngBounds.Builder()
                                .include(new LatLng(MyPosLatNum, MyPosLongNum))
                                .include(new LatLng( marker.getPosition().latitude,
                                        marker.getPosition().longitude)).build();
                        Point point = new Point();
                        getWindowManager().getDefaultDisplay().getSize(point);
                        mMap.animateCamera(CameraUpdateFactory.zoomIn());
                        Measurement();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RetryPolicy retryPolicy = new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }

    private List<LatLng> decodePoly(String encoded){
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b > 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }


    @Override
    public void onInfoWindowClick(final Marker marker) {
        final AlertDialog.Builder builder1 = new AlertDialog.Builder(MapActivity.this);
        builder1.setMessage("Do you want to save location to saved landmarks?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        tempLandmark = searchString;
                        String StringForLandmark = searchString;


                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("StringLandmarks").child(tempLandmark)
                                .setValue(StringForLandmark).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                        }
                                        else{
                                            Toast.makeText(MapActivity.this, "Landmark has not been added", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });


                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Landmarks").child(tempLandmark).child("Name")
                                .setValue(tempLandmark).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                        }
                                        else{
                                            Toast.makeText(MapActivity.this, "Name has not been added", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert1 = builder1.create();
        alert1.show();

            final AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
            builder.setMessage("Determine route to location?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                            direction(marker);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();


        }





/*
--------------------------------------------------- google places API autocomplete suggestions-----------------------
 */
private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        hideSoftKeyboard();
        final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
        final String placeId = item.getPlaceId();

        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient,placeId);
        placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
    }
};

private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>(){
    @Override
    public void onResult(@NonNull PlaceBuffer places){
        if(!places.getStatus().isSuccess()){
            Log.d(TAG, "onResult: Place query did not complete successfully." + places.getStatus().toString());
            places.release();
            return;
        }
        final Place place = places.get(0);
        try{



        mPlace = new PlaceInfo();
        mPlace.setName(place.getName().toString());
        mPlace.setAddress(place.getAddress().toString());
        //mPlace.setAttributions(place.getAttributions().toString());
        mPlace.setId(place.getId());
        mPlace.setLatlng(place.getLatLng());
        mPlace.setRating(place.getRating());
        mPlace.setPhoneNumber(place.getPhoneNumber().toString());
        mPlace.setWebsiteUri(place.getWebsiteUri());

            Log.d(TAG, "onResult: place:" + mPlace.toString());
        }catch(NullPointerException e){
            Log.e(TAG, "onResult: NullPointerException: " + e.getMessage());
        }
        moveCamera(new LatLng(place.getViewport().getCenter().latitude, place.getViewport().getCenter().longitude) , DEFAULT_ZOOM, mPlace);

        places.release();
    }

};

    private class PlaceTask extends AsyncTask<String,Integer,String>
    {

        @Override
        protected String doInBackground (String...strings) {
                String data= null;
                try{
                    //Initialize data
                    data =downloadUrl(strings[0]);
                }catch(IOException e){
                    e.printStackTrace();
                }

                return data;
            }




        @Override
        protected void onPostExecute(String s) {
                new ParserTask().execute(s);

        }

        private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>> {
            @Override
            protected List<HashMap<String,String>> doInBackground(@NonNull String...strings){

                    //Create json parser class
                    JsonParser jsonParser = new JsonParser();
                    //Initialize hash map list
                    List<HashMap<String, String>> mapList = null;
                    JSONObject object = null;
                    try{
                        //IInitialise json object
                        object =new JSONObject(strings[0]);
                        //Parse json object
                        mapList = jsonParser.parseResult(object);
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                    return mapList;
                }

            @Override
            protected void onPostExecute(@NonNull List<HashMap<String, String>> hashMaps) {
                    //clear map
                mMap.clear();
                    for(int i=0; i<hashMaps.size(); i++){
                        //Initialize hashmap
                        HashMap<String, String> hashMapList =  hashMaps.get(i);
                        //get latitude
                        double lat = Double.parseDouble(hashMapList.get("lat"));
                        double lng = Double.parseDouble(hashMapList.get("lng"));
                        //getname
                        String name = hashMapList.get("name");
                        //concat latitude and longitude
                        LatLng latLng = new LatLng(lat, lng);
                        //intialise marker options
                        MarkerOptions options = new MarkerOptions();
                        //set position
                        options.position(latLng);

                        //set title
                        options.title(name);
                        //add marker on map
                        mMap.addMarker(options);
                    }
                }
            }
        }
    }

