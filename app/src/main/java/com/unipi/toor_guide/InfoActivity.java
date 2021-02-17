package com.unipi.toor_guide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.NumberPicker;
import android.widget.TextView;

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

public class InfoActivity extends AppCompatActivity {

    TextView textView,description;
    private StorageReference storageRef;
    private FirebaseRemoteConfig remoteConfig;
    private ViewPager2 viewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        viewPager2 = findViewById(R.id.viewpager);
        List<Bitmap> images = new ArrayList<>();
        textView = findViewById(R.id.id);
        description = findViewById(R.id.description);
        String name = getIntent().getStringExtra("id");
        String imgname = getIntent().getStringExtra("path");

        textView.setText(name);
        description.setText(getIntent().getStringExtra("description"));

        storageRef = FirebaseStorage.getInstance().getReference();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //night mode ui is not supported

        remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setDefaultsAsync(R.xml.config_settings);
        remoteConfig.fetch(3).addOnCompleteListener(task -> remoteConfig.fetchAndActivate());



        try{
            File localfile = File.createTempFile("tmp","jpg") ;

            StorageReference imgref = storageRef.child("img/"+ imgname +"/");
            for(int i=0; i<3; i++){
                Log.i("image",(new StringBuilder().append(imgname).append(i+1)).toString());
                StorageReference img = imgref.child((new StringBuilder().append(imgname).append(i+1).append(".jpg")).toString());
                img.getFile(localfile).addOnSuccessListener(taskSnapshot -> {
                    images.add(BitmapFactory.decodeFile(localfile.getAbsolutePath()));

                });
            }
            viewPager2.setAdapter(new ImageAdapter(images,viewPager2));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}