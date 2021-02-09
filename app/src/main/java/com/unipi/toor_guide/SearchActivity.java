package com.unipi.toor_guide;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SearchActivity extends AppCompatActivity {
    BottomNavigationView bottomBar;

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
            if(item.getItemId()==R.id.Tours){
                startActivity(new Intent(getApplicationContext(),CreateTourActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            return item.getItemId() == R.id.search;
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }
}