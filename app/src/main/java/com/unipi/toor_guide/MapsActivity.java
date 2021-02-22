package com.unipi.toor_guide;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private LatLng loc, current_loc;
    private String name;
    TextView text_name, coord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        text_name = findViewById(R.id.name);
        coord = findViewById(R.id.coord);

        name = getIntent().getStringExtra("name");
        Bundle b = getIntent().getExtras();
        loc = (LatLng) b.get("loc");
        text_name.setText(name);

        StringBuilder gps_address = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault()); //the sms needs the address of the user to be in greek
            List<Address> addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                gps_address.append(address.getAddressLine(0));
                String[] gps = gps_address.toString().split(",");

                // we only need the road, number and city of the address
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

        // Add a marker in Sydney and move the camera

        mMap.addMarker(new MarkerOptions().position(loc).title(name+" is here."));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
        /*
        if (current_loc!=null){
            MarkerOptions my_options = new MarkerOptions().position(current_loc).title("You are here.");
            my_options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            mMap.addMarker(my_options);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current_loc,5));
        }
*/
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location!=null){
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