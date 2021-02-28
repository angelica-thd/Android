package com.unipi.toor_guide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
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
import java.util.Locale;


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
    TextView beach_textview;
    TextView village_textview;
    HorizontalScrollView beaches_hsv;
    HorizontalScrollView villages_hsv;
    LinearLayout beaches_cardholder;
    LinearLayout villages_cardholder;

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
                startActivity(new Intent(getApplicationContext(), FavouritesActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return item.getItemId() == R.id.home;
        });

        beach_textview=findViewById(R.id.beach_textview1);
        village_textview=findViewById(R.id.villages_textview1);

        beaches_hsv=findViewById(R.id.beaches_hsv1);
        villages_hsv=findViewById(R.id.villages_hsv1);

        beaches_cardholder=findViewById(R.id.beaches_cardholder1);
        villages_cardholder=findViewById(R.id.villages_cardholder1);

        llayoutparams = new LinearLayout.LayoutParams(
                300,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        llayoutparams.setMargins(20,0,20,0);



        List<String> beach_names = new ArrayList<String>();
        List<String> gr_beach_names = new ArrayList<String>();
        List<String> villages_names = new ArrayList<String>();
        List<String> gr_villages_names = new ArrayList<String>();

        List<String>  beaches_desc = new ArrayList<>();
        List<String>  villages_desc = new ArrayList<>();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                boolean hasSights = false;

                Log.i("datasnapshot","cool");
                for (DataSnapshot nswe : snapshot.getChildren()){
                    for(DataSnapshot  snap : nswe.child("Beaches").getChildren()){
                        String beachen = String.valueOf(snap.getKey());
                        String beachel = String.valueOf(snap.child("NameTrans").getValue());
                        Log.i("Beach el",beachel);
                        if(!beach_names.contains(beachen)) {
                            beach_names.add(beachen);
                            gr_beach_names.add(beachel);
                            beaches_desc.add(String.valueOf(snap.child("Info").getValue()));
                        }
                        Log.i("names", String.valueOf(beach_names));
                    }
                    for (DataSnapshot snap : nswe.child("Villages").getChildren()) {
                        String villagen = String.valueOf(snap.getKey());
                        String villagel=String.valueOf(snap.child("NameTrans").getValue());
                        if(!villages_names.contains(villagen)) {
                            villages_names.add(villagen);
                            gr_villages_names.add(villagel);
                            villages_desc.add(String.valueOf(snap.child("Info").getValue()));
                        }
                    }
                    Log.i("vill el", String.valueOf(villages_names));
                }


                 for(int i=0; i<beach_names.size(); i++){
                    count ++;
                    String imgname = beach_names.get(i).toLowerCase();
                    if (imgname.contains(" ")) imgname = imgname.replace(" ","_");
                    String imgpath = imgname;
                    imgname = (new StringBuilder().append(imgname).append(".jpg")).toString();
                    Log.i("imagename",imgname);

                    try{
                        File localfile = File.createTempFile("tmp","jpg") ;
                        StorageReference imgref = storageRef.child("img/"+ imgname);
                        int finalI = i;
                        String finalImgname = imgname;
                        imgref.getFile(localfile).addOnSuccessListener(taskSnapshot ->cards(mainactivity,
                                BitmapFactory.decodeFile(localfile.getAbsolutePath()),
                                beach_names.get(finalI),
                                beaches_desc.get(finalI),
                                finalImgname,
                                false,
                                beaches_cardholder,
                                gr_beach_names.get(finalI)));

                    }catch (IOException e){
                        e.printStackTrace();
                    }
            }

             for(int i=0; i<villages_names.size(); i++){
                count ++;
                String imgname = villages_names.get(i).toLowerCase();
                if (imgname.contains(" ")) imgname = imgname.replace(" ","_");
                String imgpath = imgname;
                imgname = (new StringBuilder().append(imgname).append(".jpg")).toString();
                Log.i("imagename",imgname);
                try{
                    File localfile = File.createTempFile("tmp","jpg") ;
                    StorageReference imgref = storageRef.child("img/"+ imgname);
                    int finalI = i;
                    String finalImgname = imgname;
                    imgref.getFile(localfile).addOnSuccessListener(taskSnapshot ->cards(mainactivity,
                            BitmapFactory.decodeFile(localfile.getAbsolutePath()),
                            villages_names.get(finalI),
                            villages_desc.get(finalI),
                            finalImgname,
                            true,
                            villages_cardholder,
                            gr_villages_names.get(finalI)));

                }catch (IOException e){
                    e.printStackTrace();
                }
            }
                int progress =  Math.round(count/(beach_names.size()+villages_names.size()));
                if(progress<1){
                    progressBar.setProgress(progress);
                    Log.i("progress",String.valueOf(progress));
                }
                else {
                    ConstraintLayout layout = findViewById(R.id.mainLayout);
                    layout.setAlpha(1);
                    progressBar.setVisibility(View.INVISIBLE);
                }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.i("fire_error",error.getMessage());
        }
    });

    }


    public void cards(Context context, Bitmap background, String name,String description, String imgpath,boolean village,LinearLayout cardholder,String gr_name){
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
        if (name.contains(" ")) name = name.replace(" ", "\n");
        String[] systemLangs = Resources.getSystem().getConfiguration().getLocales().toLanguageTags().split(",");

        if (systemLangs[0].contains(Locale.forLanguageTag("EL").toLanguageTag())) cardtext.setText(gr_name);
        else cardtext.setText(name);

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
                    putExtra("village",village).
                    putExtra("gr_name",gr_name));
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