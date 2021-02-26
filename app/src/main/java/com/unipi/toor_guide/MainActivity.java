package com.unipi.toor_guide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import android.view.View;
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
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context mainactivity = this;
        progressBar = findViewById(R.id.progress_view);
        progressBar.setVisibility(View.VISIBLE);
        storageRef = FirebaseStorage.getInstance().getReference();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //night mode ui is not supported

        remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setDefaultsAsync(R.xml.config_settings);
        remoteConfig.fetch(3).addOnCompleteListener(task -> remoteConfig.fetchAndActivate());


        firedb = FirebaseDatabase.getInstance();
        ref = firedb.getReference();


        bottomBar = findViewById(R.id.bottombar);
        bottomBar.getMenu().getItem(0).setChecked(true);
        bottomBar.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.search) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            if (item.getItemId() == R.id.Favourites) {
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
        List<String> desc = new ArrayList<>();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                boolean hasSights = false;
                Log.i("datasnapshot","cool");
                for (DataSnapshot snap: snapshot.getChildren()){
                    for(DataSnapshot  sight : snap.child("Beaches").getChildren()){
                        String beach = String.valueOf(sight.getKey());
                        if(!names.contains(beach))  names.add(beach);
                        desc.add(String.valueOf(sight.child("Info").getValue()));
                        hasSights = false;
                    }
                    for (DataSnapshot sight : snap.child("Villages").getChildren()) {
                        String village = String.valueOf(sight.getKey());
                        if(!names.contains(village))  names.add(village);
                        desc.add(String.valueOf(sight.child("Info").getValue()));
                        hasSights = true;
                    }
                    Log.i("storage",String.valueOf(names));
                }

                for(int i=0; i<names.size(); i++){
                    count+=1;
                    int progress =  Math.round(count/names.size());

                    String imgname = names.get(i).toLowerCase();
                    if (imgname.contains(" ")) imgname = imgname.replace(" ","_");
                    String imgpath = imgname;
                    imgname = (new StringBuilder().append(imgname).append(".jpg")).toString();
                    Log.i("imagename",imgname);
                    try{
                        File localfile = File.createTempFile("tmp","jpg") ;
                        StorageReference imgref = storageRef.child("img/"+ imgname);
                        int finalI = i;
                        String finalImgname = imgname;
                        boolean finalVill = hasSights;
                        imgref.getFile(localfile).addOnSuccessListener(taskSnapshot ->cards(mainactivity,
                                BitmapFactory.decodeFile(localfile.getAbsolutePath()),
                                names.get(finalI),
                                desc.get(finalI),
                                finalImgname,
                                finalVill));

                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    if(progress<1){
                        progressBar.setProgress(progress);
                        Log.i("progress",String.valueOf(progress));
                    }
                    else progressBar.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("fire_error",error.getMessage());
            }
        });
    }


    public void cards(Context context, Bitmap background, String name,String description, String imgpath,boolean village){
        cardview=new CardView(context);
        cardview.setRadius(0);
        cardview.setPadding(0, 0, 0, 0);
        cardview.setPreventCornerOverlap(false);
        cardview.setBackgroundResource(R.drawable.rectangled);


        cardimage=new ImageView(context);
        cardimage.setImageBitmap(background);
        cardimage.setScaleType(ImageView.ScaleType.FIT_XY);
        cardimage.setMaxHeight(Integer.MAX_VALUE);
        cardimage.setMinimumHeight(Integer.MAX_VALUE);
        cardimage.setMaxWidth(Integer.MAX_VALUE);
        cardimage.setMinimumHeight(Integer.MAX_VALUE);
        cardview.addView(cardimage);

        cardtext = new TextView(context);
        cardtext.setText(name);
        cardtext.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        cardtext.setTextColor(Color.WHITE);
        cardtext.setPadding(10,430,10,0);
        cardtext.setGravity(Gravity.END);

        cardview.addView(cardtext);
        String finalName = name;
        cardview.setOnClickListener(v -> {
            startActivity(new Intent(this,InfoActivity.class).putExtra("id", finalName).
                    putExtra("description",description).
                    putExtra("path",imgpath).
                    putExtra("village",village));
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

    @Override
    protected void onResume() {
        super.onResume();
        bottomBar.getMenu().getItem(0).setChecked(true);
    }

}