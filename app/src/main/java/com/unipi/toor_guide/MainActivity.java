package com.unipi.toor_guide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private long backPressedTime;
    private Toast backToast;
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

        FloatingActionButton fab = findViewById(R.id.settingsbutton);
        fab.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), SettingsActivity.class)));


        bottomBar=findViewById(R.id.bottombar);
        bottomBar.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId()==R.id.home){
                return true;
            }
            if(item.getItemId()==R.id.history){
                startActivity(new Intent(getApplicationContext(),HistoryActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            if(item.getItemId()==R.id.Tours){
                startActivity(new Intent(getApplicationContext(), CreateTourActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            return false;
        });

        cardholder=findViewById(R.id.cardholder);
        llayoutparams = new LinearLayout.LayoutParams(
                300,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        llayoutparams.setMargins(20,0,20,0);
        for (int i=0; i<7;i++){
            cardview=new CardView(this);
            cardview.setRadius(0);
            cardview.setPadding(0, 0, 0, 0);
            cardview.setPreventCornerOverlap(false);
            cardview.setBackgroundResource(R.drawable.rectangled);

            cardimage=new ImageView(this);
            cardimage.setBackgroundResource(R.drawable.testing_img);
            cardimage.setScaleType(ImageView.ScaleType.FIT_XY);
            cardview.addView(cardimage);

            cardtext = new TextView(this);
            cardtext.setText(R.string.sights_textview);
            cardtext.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            cardtext.setTextColor(Color.WHITE);
            cardtext.setPadding(0,430,60,0);
            cardtext.setGravity(Gravity.END);

            cardview.addView(cardtext);

            cardview.setOnClickListener(v -> {

            });

            cardholder.addView(cardview,llayoutparams);
        }
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