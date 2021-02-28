package com.unipi.toor_guide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InfoActivity extends AppCompatActivity implements SensorEventListener {

    TextView textView,description,what2see;
    ImageButton findBeach ,fav;
    private StorageReference storageRef;
    private FirebaseRemoteConfig remoteConfig;
    private FirebaseDatabase firedb;
    private DatabaseReference ref;
    private Sight s;

    private SensorManager mSensorManager;
    private Sensor mTemperatureSensor;
    private boolean isTemperatureSensorPresent;
    private boolean flag=false;

    ImageView info_img;
    ListView sightList;

    private boolean isFavourite,hasSights ;
    private String name;
    private SQLiteDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        hasSights = getIntent().getBooleanExtra("village",false);
        sightList = findViewById(R.id.listView);
        findBeach =  findViewById(R.id.find_sight_on_map);

        what2see = findViewById(R.id.what2see_text);
        fav = findViewById(R.id.favButton);
        firedb = FirebaseDatabase.getInstance();
        ref = firedb.getReference();

        //SQLite database initialization
        db = openOrCreateDatabase("FavouriteSights", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS favs(name TEXT)");


        info_img = findViewById(R.id.info_img);

        isFavourite = false; //should get from user info

        textView = findViewById(R.id.id);
        description = findViewById(R.id.description);



        if(hasSights) findBeach.setClickable(false);
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mTemperatureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        String en_desc = getIntent().getStringExtra("description").split("GR")[0];
        String gr_desc = getIntent().getStringExtra("description").split("GR")[1];
        if(en_desc.contains(".,") || en_desc.contains("{EN=")) {
            en_desc= en_desc.replace("{EN=","");
            en_desc=en_desc.replace(".,",".");
        }
        if(gr_desc.contains("GR=") || gr_desc.contains("}")){
            gr_desc= gr_desc.replace("GR=","");
            gr_desc = gr_desc.replace("}","");
            gr_desc = gr_desc.replace("=","");
            Log.i("grnmae",gr_desc);
        }
        name = getIntent().getStringExtra("id");
        String imgname = getIntent().getStringExtra("path");


        Cursor cursor = db.rawQuery("select * from favs ", new String[]{});
        if(cursor.getCount()>0) {
            while (cursor.moveToNext()) {
                if (name.contains("\n")) name = name.replace("\n", " ");
                if (cursor.getString(0).contains(name)) {
                    Log.i("name", name);
                    isFavourite = true;
                }
            }
        }
        Log.i("name",name);
        if (isFavourite){
           fav.setImageDrawable(getDrawable(R.drawable.ic_baseline_favorite_24));
        }

        Log.i("lang", Resources.getSystem().getConfiguration().getLocales().toLanguageTags()+ Locale.forLanguageTag("EL").toLanguageTag());
        String[] systemLangs = Resources.getSystem().getConfiguration().getLocales().toLanguageTags().split(",");

        if (systemLangs[0].contains(Locale.forLanguageTag("EL").toLanguageTag())){
            description.setText(gr_desc);
            String gr_name = getIntent().getStringExtra("gr_name");
            if(gr_name.contains(" ")) {
                gr_name = gr_name.replace(" ","\n");
            }
            textView.setText(gr_name);
        }
        else{
            description.setText(en_desc);
            if(name.contains(" ")) name = name.replace(" ","\n");
            textView.setText(name);

        }



        s = new Sight();
        SightsAdapter adapter = new SightsAdapter(this, R.layout.listview_adapter,  s.getsights(ref,name));
        sightList.setAdapter(adapter);


        storageRef = FirebaseStorage.getInstance().getReference();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //night mode ui is not supported

        remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setDefaultsAsync(R.xml.config_settings);
        remoteConfig.fetch(3).addOnCompleteListener(task -> remoteConfig.fetchAndActivate());


        try{
            File localfile = File.createTempFile("tmp","jpg") ;
            StorageReference imgref = storageRef.child("img/"+ imgname);
            imgref.getFile(localfile).addOnSuccessListener(taskSnapshot -> info_img.setImageBitmap(BitmapFactory.decodeFile(localfile.getAbsolutePath())));
        }catch (IOException e){
            e.printStackTrace();
        }

        if(mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
            mTemperatureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            isTemperatureSensorPresent = true;
        }
    }


    public boolean un_favourite(){
        if(isFavourite) isFavourite=false; //unfavourite
        else isFavourite=true; //favourite
        return isFavourite;
    }

    public void favourite(View view){
        boolean exists = false;
        un_favourite();
        if(isFavourite){
            if(name.contains("\n")) name = name.replace("\n"," ");
            fav.setImageDrawable(getDrawable(R.drawable.ic_baseline_favorite_24));

            Cursor cursor = db.rawQuery("select * from favs ", new String[]{});
            if(cursor.getCount()>0) {
                while (cursor.moveToNext())
                    if(!cursor.getString(0).equals(name)){
                        exists = false;
                    }else exists = true;
            }
            if (!exists) db.execSQL("insert into favs values('"+name+"')");
        }else{
            fav.setImageDrawable(getDrawable(R.drawable.ic_baseline_favorite_border_24));
            db.execSQL("delete from favs where name ='"+name +"'");
        }
    }



    public void find(View view){
        if(name.contains("\n")) name = name.replace("\n"," ");
        ref.addValueEventListener(new ValueEventListener() {
            Sight sight = null;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap: snapshot.getChildren()){
                    for(DataSnapshot  s : snap.child("Beaches").getChildren()){
                        String beach = String.valueOf(s.getKey());
                        Log.i("sight/beachname",beach);
                        if(beach.equals(name)){
                            String x = String.valueOf(s.child("Location").child("X").getValue());
                            String y = String.valueOf(s.child("Location").child("Y").getValue());
                            LatLng loc =new LatLng(Double.parseDouble(x),Double.parseDouble(y));
                            sight = new Sight(name,String.valueOf(s.child("Info").child("EN").getValue()),
                                    String.valueOf(s.child("NameTrans").getValue()),
                                    String.valueOf(s.child("Info").child("GR").getValue()),
                                    loc,null);
                            startActivity(new Intent(InfoActivity.this,MapsActivity.class).
                                    putExtra("name",name).
                                    putExtra("loc",loc));
                            Log.i("sight", name);

                            break;
                        }
                    }
                    if(sight!=null) break;
                    for (DataSnapshot s: snap.child("Villages").getChildren()) {
                        String village = String.valueOf(s.getKey());
                        if(village.equals(name)) {
                            if(s.child("Location").exists()){
                                String x = String.valueOf(s.child("Location").child("X").getValue());
                                String y = String.valueOf(s.child("Location").child("Y").getValue());
                                LatLng loc =new LatLng(Double.parseDouble(x),Double.parseDouble(y));
                                sight = new Sight(name,String.valueOf(s.child("Info").child("EN").getValue()),
                                        String.valueOf(s.child("NameTrans").getValue()),
                                        String.valueOf(s.child("Info").child("GR").getValue()),
                                        loc,null);
                                startActivity(new Intent(InfoActivity.this,MapsActivity.class).
                                        putExtra("name",name).
                                        putExtra("loc",loc));
                                break;
                            }
                        }
                    }
                    if(sight!=null) break;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("fire_error",error.getMessage());
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        if(isTemperatureSensorPresent) {
            mSensorManager.registerListener(this, mTemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isTemperatureSensorPresent) {
            mSensorManager.unregisterListener(this);
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if(!hasSights && event.values[0]<20 && !flag) {
            //Toast.makeText(this,getString(R.string.toocold),Toast.LENGTH_LONG).show();
            flag = true;
            Snackbar.make(findViewById(R.id.constraintLayout), getString(R.string.toocold), Snackbar.LENGTH_LONG).show();
        }
        if(event.values[0]>20 && flag){
            flag=false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}