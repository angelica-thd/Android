package com.unipi.toor_guide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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

public class FavouritesActivity extends AppCompatActivity {
    BottomNavigationView bottomBar;
    Context favouritesactivity = this;
    private StorageReference storageRef;
    private FirebaseRemoteConfig remoteConfig;
    private FirebaseDatabase firedb;
    private DatabaseReference ref;
    private Integer wascalled = 0;

    private SQLiteDatabase db;
    private List<String> fav_list=new ArrayList<>();
    TextView no_fav_yet;

    LinearLayout cardholder;
    CardView cardview;
    LinearLayout.LayoutParams llayoutparams;
    ImageView cardimage;
    TextView cardtext;

    List<String> fav_names= new ArrayList<>();
    List<String> gr_fav_names= new ArrayList<>();
    List<String>  fav_desc= new ArrayList<>();
    List<Boolean> hasSights= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        //bottom bar
        bottomBar=findViewById(R.id.bottombar);
        bottomBar.getMenu().getItem(2).setChecked(true);
        bottomBar.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId()==R.id.home){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            if(item.getItemId()==R.id.search){
                startActivity(new Intent(getApplicationContext(),SearchActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            return item.getItemId() == R.id.Favourites;
        });



        //layout
        llayoutparams = new LinearLayout.LayoutParams(
                550,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        llayoutparams.setMargins(10,0,10,0);

        no_fav_yet=findViewById(R.id.no_favourites_textview);
        cardholder=findViewById(R.id.fav_llayout);




        storageRef = FirebaseStorage.getInstance().getReference();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //night mode ui is not supported

        remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setDefaultsAsync(R.xml.config_settings);
        remoteConfig.fetch(3).addOnCompleteListener(task -> remoteConfig.fetchAndActivate());

        firedb = FirebaseDatabase.getInstance();
        ref = firedb.getReference();


        db = openOrCreateDatabase("FavouriteSights", Context.MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS favs(name TEXT)");


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("logs/onresume","on resume was called");
        bottomBar.getMenu().getItem(2).setChecked(true);
        fav_list.clear();
        if(cardholder.getChildCount()>0) cardholder.removeAllViews();
        fav_names.clear();
        gr_fav_names.clear();
        hasSights.clear();

        fav_list_update_fun();
        if(fav_list.size()==0){
            no_fav_yet.setVisibility(View.VISIBLE);
        }
        else{
            no_fav_yet.setVisibility(View.INVISIBLE);
            addcards();
        }
    }

    //gets all the favs from the db, puts them to list
    private void fav_list_update_fun(){
        fav_list.clear();
        Cursor cursor = db.rawQuery("select * from favs ", new String[]{});
        if(cursor.getCount()>0){
            while (cursor.moveToNext())
                fav_list.add(cursor.getString(0));
        }
    }

    private void addcards(){
        Log.i("logs/addcards","addcards was called ");
        wascalled+=1;
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap: snapshot.getChildren()){
                    for(DataSnapshot  sight : snap.child("Beaches").getChildren()){
                        String beachen = String.valueOf(sight.getKey());
                        String beachel = String.valueOf(sight.child("NameTrans").getValue());
                        if(fav_list.contains(beachen) && !fav_names.contains(beachen)) {
                            fav_names.add(beachen);
                            gr_fav_names.add(beachel);
                            fav_desc.add(String.valueOf(sight.child("Info").getValue()));
                            hasSights.add(false);
                        }
                    }
                    for (DataSnapshot sight : snap.child("Villages").getChildren()) {
                        String villagen = String.valueOf(sight.getKey());
                        String villagel=String.valueOf(sight.child("NameTrans").getValue());
                        if(fav_list.contains(villagen) && !fav_names.contains(villagen)) {
                            fav_names.add(villagen);
                            gr_fav_names.add(villagel);
                            fav_desc.add(String.valueOf(sight.child("Info").getValue()));
                            hasSights.add(true);
                        }
                    }
                }
                Log.i("logs/fav_names",String.valueOf(fav_names)+fav_list);

                for(int i=0; i<fav_names.size(); i++){
                    String imgname = fav_names.get(i).toLowerCase();
                    if (imgname.contains(" ")) imgname = imgname.replace(" ","_");
                    String imgpath = imgname;
                    imgname = (new StringBuilder().append(imgname).append(".jpg")).toString();
                    try{
                        File localfile = File.createTempFile("tmp","jpg") ;
                        StorageReference imgref = storageRef.child("img/"+ imgname);
                        int finalI = i;
                        String finalImgname = imgname;
                        imgref.getFile(localfile).addOnSuccessListener(taskSnapshot ->cards(favouritesactivity,
                                BitmapFactory.decodeFile(localfile.getAbsolutePath()),
                                fav_names.get(finalI),
                                fav_desc.get(finalI),
                                finalImgname,
                                hasSights.get(finalI),
                                cardholder,
                                gr_fav_names.get(finalI)));

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

    public void cards(Context context, Bitmap background, String name, String description, String imgpath, boolean village, LinearLayout cardholder, String gr_name) {
        cardview = new CardView(context);
        cardview.setRadius(0);
        cardview.setPadding(0, 0, 0, 0);
        cardview.setPreventCornerOverlap(false);
        cardview.setBackgroundResource(R.drawable.rectangled);

        cardimage = new ImageView(context);
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
        cardtext.setPadding(10, 1200, 50, 0);
        cardtext.setGravity(Gravity.END);

        cardview.addView(cardtext);
        String finalName = name;
        cardview.setOnClickListener(v -> {
            startActivity(new Intent(this, InfoActivity.class).
                    putExtra("id", finalName).
                    putExtra("description", description).
                    putExtra("path", imgpath).
                    putExtra("village",village).
                    putExtra("gr_name",gr_name));
        });

        cardholder.addView(cardview, llayoutparams);
    }


}