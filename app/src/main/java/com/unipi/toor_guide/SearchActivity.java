package com.unipi.toor_guide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class SearchActivity extends AppCompatActivity {
    BottomNavigationView bottomBar;
    EditText search_text;
    Button search_button;

    Context searchactivity = this;
    private StorageReference storageRef;
    private FirebaseRemoteConfig remoteConfig;
    private FirebaseDatabase firedb;
    private DatabaseReference ref;

    List<String> beach_names = new ArrayList<String>();
    List<String> gr_beach_names = new ArrayList<String>();
    List<String> villages_names = new ArrayList<String>();
    List<String> gr_villages_names = new ArrayList<String>();

    List<String>  beaches_desc = new ArrayList<>();
    List<String>  villages_desc = new ArrayList<>();

    MyTts myTts;
    private static final int REC_RESULT = 653;

    TextView beach_textview;
    TextView village_textview;
    HorizontalScrollView beaches_hsv;
    HorizontalScrollView villages_hsv;
    LinearLayout beaches_cardholder;
    LinearLayout villages_cardholder;
    CardView cardview;
    LinearLayout.LayoutParams llayoutparams;
    ImageView cardimage;
    TextView cardtext;

    TextView no_beaches;
    TextView no_villages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        bottomBar=findViewById(R.id.bottombar);
        bottomBar.getMenu().getItem(1).setChecked(true);
        bottomBar.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId()==R.id.home){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            if(item.getItemId()==R.id.Favourites){
                startActivity(new Intent(getApplicationContext(),FavouritesActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            return item.getItemId() == R.id.search;
        });

        beach_textview=findViewById(R.id.beach_textview);
        village_textview=findViewById(R.id.villages_textview);

        beaches_hsv=findViewById(R.id.beaches_hsv);
        villages_hsv=findViewById(R.id.villages_hsv);

        beaches_cardholder=findViewById(R.id.beaches_cardholder);
        villages_cardholder=findViewById(R.id.villages_cardholder);

        llayoutparams = new LinearLayout.LayoutParams(
                300,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        llayoutparams.setMargins(20,0,20,0);

        search_text=findViewById(R.id.search_edittext);
        search_button=findViewById(R.id.search_button);
        search_button.setOnLongClickListener(view -> {
            recognize();
            search_fun();
            return true;
        });

        storageRef = FirebaseStorage.getInstance().getReference();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //night mode ui is not supported

        remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setDefaultsAsync(R.xml.config_settings);
        remoteConfig.fetch(3).addOnCompleteListener(task -> remoteConfig.fetchAndActivate());

        firedb = FirebaseDatabase.getInstance();
        ref = firedb.getReference();

        myTts = new MyTts(searchactivity);

        no_beaches=findViewById(R.id.no_beach_textview);
        no_villages=findViewById(R.id.no_villages_textview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomBar.getMenu().getItem(1).setChecked(true);
        beach_names.clear();
        beaches_desc.clear();
        villages_names.clear();
        villages_desc.clear();
        beaches_cardholder.removeAllViews();
        villages_cardholder.removeAllViews();
        beach_textview.setVisibility(View.INVISIBLE);
        village_textview.setVisibility(View.INVISIBLE);
        beaches_hsv.setVisibility(View.INVISIBLE);
        villages_hsv.setVisibility(View.INVISIBLE);
        no_beaches.setVisibility(View.INVISIBLE);
        no_villages.setVisibility(View.INVISIBLE);
    }

    public void search(View view){
        if(beach_textview.getVisibility()==View.INVISIBLE){
            beach_textview.setVisibility(View.VISIBLE);
            village_textview.setVisibility(View.VISIBLE);
            beaches_hsv.setVisibility(View.VISIBLE);
            villages_hsv.setVisibility(View.VISIBLE);
        }
        search_fun();
    }

    private void search_fun(){
        beach_names.clear();
        gr_beach_names.clear();
        beaches_desc.clear();
        villages_names.clear();
        gr_villages_names.clear();
        villages_desc.clear();
        beaches_cardholder.removeAllViews();
        villages_cardholder.removeAllViews();
        if(!search_text.getText().toString().equals("")){
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snap: snapshot.getChildren()){
                        for(DataSnapshot  sight : snap.child("Beaches").getChildren()){
                            String beachen = String.valueOf(sight.getKey());
                            String beachel = String.valueOf(sight.child("NameTrans").getValue());
                            if((beachen.toLowerCase().contains(search_text.getText().toString().toLowerCase())||beachel.toLowerCase().contains(search_text.getText().toString().toLowerCase()))&&!beach_names.contains(beachen)) {
                                beach_names.add(beachen);
                                gr_beach_names.add(beachel);
                                beaches_desc.add(String.valueOf(sight.child("Info").getValue()));
                            }
                        }
                        for (DataSnapshot sight : snap.child("Villages").getChildren()) {
                            String villagen = String.valueOf(sight.getKey());
                            String villagel=String.valueOf(sight.child("NameTrans").getValue());
                            if((villagen.toLowerCase().contains(search_text.getText().toString().toLowerCase())||villagel.toLowerCase().contains(search_text.getText().toString().toLowerCase()))&&!villages_names.contains(villagen)) {
                                villages_names.add(villagen);
                                gr_villages_names.add(villagel);
                                villages_desc.add(String.valueOf(sight.child("Info").getValue()));
                            }
                        }
                    }

                 
                    if(beach_names.isEmpty()){
                        no_beaches.setVisibility(View.VISIBLE);
                        no_beaches.setText(getText(R.string.no_beach)+search_text.getText().toString());
                    }else {
                        no_beaches.setVisibility(View.INVISIBLE);
                    }

                    if(villages_names.isEmpty()){
                        no_villages.setVisibility(View.VISIBLE);
                        no_villages.setText(getText(R.string.no_villages)+search_text.getText().toString());
                    }else{
                        no_villages.setVisibility(View.INVISIBLE);
                    }

                    for(int i=0; i<beach_names.size(); i++){

                        String imgname = beach_names.get(i).toLowerCase();
                        if (imgname.contains(" ")) imgname = imgname.replace(" ","_");
                        String imgpath = imgname;
                        imgname = (new StringBuilder().append(imgname).append(".jpg")).toString();

                        try{
                            File localfile = File.createTempFile("tmp","jpg") ;
                            StorageReference imgref = storageRef.child("img/"+ imgname);
                            int finalI = i;
                            String finalImgname = imgname;
                            imgref.getFile(localfile).addOnSuccessListener(taskSnapshot ->cards(searchactivity,
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

                        String imgname = villages_names.get(i).toLowerCase();
                        if (imgname.contains(" ")) imgname = imgname.replace(" ","_");
                        String imgpath = imgname;
                        imgname = (new StringBuilder().append(imgname).append(".jpg")).toString();
                        try{
                            File localfile = File.createTempFile("tmp","jpg") ;
                            StorageReference imgref = storageRef.child("img/"+ imgname);
                            int finalI = i;
                            String finalImgname = imgname;
                            imgref.getFile(localfile).addOnSuccessListener(taskSnapshot ->cards(searchactivity,
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
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.i("fire_error",error.getMessage());
                }
            });
        }
        else {
            beach_names.clear();
            beaches_desc.clear();
            villages_names.clear();
            villages_desc.clear();
            beaches_cardholder.removeAllViews();
            villages_cardholder.removeAllViews();
            beach_textview.setVisibility(View.INVISIBLE);
            beaches_hsv.setVisibility(View.INVISIBLE);
            village_textview.setVisibility(View.INVISIBLE);
            villages_hsv.setVisibility(View.INVISIBLE);
        }
    }

    public void cards(Context context, Bitmap background, String name, String description, String imgpath,boolean village,LinearLayout cardholder,String gr_name) {
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
        cardtext.setPadding(10, 430, 10, 0);
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }

    //Χειρισμός φωνητικών εντολων{
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REC_RESULT && resultCode==RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches.contains("favourites") || matches.contains("favorites") || matches.contains("αγαπημένα")){
                startActivity(new Intent(this, FavouritesActivity.class));
            }
            else if (matches.contains("home") ||matches.contains("sights") || matches.contains("αξιοθέατα") || matches.contains("αρχική")){
                startActivity(new Intent(this, MainActivity.class));
            }
            else{
                search_text.setText(matches.get(0));
            }
        }

    }

    public void recognize(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"What are you searching?");
        startActivityForResult(intent,REC_RESULT);
    }
    //}
}