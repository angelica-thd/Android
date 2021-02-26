package com.unipi.toor_guide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LatLng loc, current_loc;
    private String name;
    private LocationManager locationManager;
    TextView text_name, coord;
    FloatingActionButton fabloc_user,fabloc_point;
    private static final int  reqCode = 786;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //night mode ui is not supported

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); //location services initialization
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, reqCode);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, reqCode);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


        text_name = findViewById(R.id.name);
        coord = findViewById(R.id.coord);


        name = getIntent().getStringExtra("name");
        Bundle b = getIntent().getExtras();
        loc = (LatLng) b.get("loc");
        text_name.setText(name);

        fabloc_user = findViewById(R.id.floating_user_location);
        fabloc_point = findViewById(R.id.floating_point_location);

        fabloc_user.setOnClickListener(view -> {
            Log.i("loc",String.valueOf(current_loc));
            if(current_loc!=null){
                MarkerOptions my_options = new MarkerOptions().position(current_loc).title("You are here.");
                my_options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                mMap.addMarker(my_options);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current_loc,15));
            } else Toast.makeText(this,R.string.map_not_ready,Toast.LENGTH_LONG).show();

        });

        fabloc_point.setOnClickListener(view -> {
             if(loc!=null){
                mMap.addMarker(new MarkerOptions().position(loc).title(name+" is here."));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc,15));
            }
             else Toast.makeText(this,R.string.map_not_ready,Toast.LENGTH_LONG).show();

        });

        StringBuilder gps_address = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault()); //the sms needs the address of the user to be in greek
            List<Address> addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                gps_address.append(address.getAddressLine(0));
                coord.setText(gps_address.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (loc != null) {
           fabloc_point.performClick();
       }else Toast.makeText(this,R.string.map_not_ready,Toast.LENGTH_LONG).show();
    }



    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location!=null){
            Log.i("loc",String.valueOf(location));
            current_loc = new LatLng(location.getLatitude(),location.getLongitude());

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}