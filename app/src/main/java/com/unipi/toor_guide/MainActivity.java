package com.unipi.toor_guide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private long backPressedTime;
    private Toast backToast;
    private StorageReference storageRef;
    private FirebaseRemoteConfig remoteConfig;
    private FirebaseDatabase firedb;
    private DatabaseReference ref;

    BottomNavigationView bottomBar;

    LinearLayout cardholder;
    CardView cardview;
    LinearLayout.LayoutParams llayoutparams;
    ImageView cardimage;
    TextView cardtext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context mainactivity = this;
        storageRef = FirebaseStorage.getInstance().getReference();

        remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setDefaultsAsync(R.xml.config_settings);
        remoteConfig.fetch(3).addOnCompleteListener(task -> remoteConfig.fetchAndActivate());

        FloatingActionButton fab = findViewById(R.id.settingsbutton);
        fab.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), SettingsActivity.class)));

        firedb = FirebaseDatabase.getInstance();
        ref = firedb.getReference();
       //east = firedb.getReference("East");
       // west = firedb.getReference("West");
        //north = firedb.getReference("North");
       // south = firedb.getReference("South");


        bottomBar = findViewById(R.id.bottombar);
        bottomBar.getMenu().getItem(0).setChecked(true);
        bottomBar.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.search) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            if (item.getItemId() == R.id.Tours) {
                startActivity(new Intent(getApplicationContext(), CreateTourActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return item.getItemId() == R.id.home;
        });

        cardholder=findViewById(R.id.cardholder);
        llayoutparams = new LinearLayout.LayoutParams(
                300,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        llayoutparams.setMargins(20,0,20,0);

        List<String> names = new ArrayList<String>();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("datasnapshot","cool");
                for (DataSnapshot snap: snapshot.getChildren()){
                    for(DataSnapshot  sight : snap.child("Beaches").getChildren()){
                        String beach = String.valueOf(sight.getKey());
                        if(!names.contains(beach))  names.add(beach);
                    }
                    for (DataSnapshot sight : snap.child("Villages").getChildren()) {
                        String beach = String.valueOf(sight.getKey());
                        if(!names.contains(beach))  names.add(beach);
                    }
                    Log.i("storage",String.valueOf(names));
                }

                for(String name : names){
                    String imgname = name.toLowerCase();
                    if (imgname.contains(" ")) imgname = imgname.replace(" ","_");
                    imgname = (new StringBuilder().append(imgname).append(".jpg")).toString();
                    Log.i("imagename",imgname);
                    try{
                        File localfile = File.createTempFile("tmp","jpg");
                        StorageReference imgref = storageRef.child("img/"+ imgname);
                        imgref.getFile(localfile).addOnSuccessListener(taskSnapshot ->cards(mainactivity,BitmapFactory.decodeFile(localfile.getAbsolutePath()),name));
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("fire_error",error.getMessage());
            }
        });
    }



    public void cards(Context context, Bitmap background, String name){
        cardview=new CardView(context);
        cardview.setRadius(0);
        cardview.setPadding(0, 0, 0, 0);
        cardview.setPreventCornerOverlap(false);
        cardview.setBackgroundResource(R.drawable.rectangled);

        cardimage=new ImageView(context);
        cardimage.setImageBitmap(background);
        cardimage.setScaleType(ImageView.ScaleType.FIT_XY);
        cardimage.setMaxHeight(2147483647);
        cardimage.setMinimumHeight(2147483647);
        cardimage.setMaxWidth(2147483647);
        cardimage.setMinimumHeight(2147483647);
        cardview.addView(cardimage);

        cardtext = new TextView(context);
        if(name.contains(" ")) name = name.replace(" ","\n");
        cardtext.setText(name);
        cardtext.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        cardtext.setTextColor(Color.WHITE);
        cardtext.setPadding(10,430,20,0);
        cardtext.setGravity(Gravity.END);

        cardview.addView(cardtext);
        cardview.setOnClickListener(v -> {

        });

        cardholder.addView(cardview,llayoutparams);
    }


    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()){
            backToast.cancel();
            super.onBackPressed();
            finishAffinity();
        }else {
            backToast = Toast.makeText(getApplicationContext(),"Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

}