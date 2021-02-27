package com.unipi.toor_guide;

import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Sight {
    private String name;
    private String gr_name;
    private String info_en;
    private String info_gr;
    private LatLng location;
    private String village;

    public Sight() {
    }

    public String getGr_name() {
        return gr_name;
    }

    public void setGr_name(String gr_name) {
        this.gr_name = gr_name;
    }

    public Sight(String name, String gr_name, String info_en, String info_gr, LatLng location, String village) {
        this.name = name;
        this.gr_name = gr_name;
        this.info_en = info_en;
        this.info_gr = info_gr;
        this.location = location;
        this.village = village;
    }

    public List<Sight> getsights(DatabaseReference ref, String name){
        List<Sight> sights = new ArrayList<>();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Log.i("datasnapshot","cool");
                for (DataSnapshot snap: snapshot.getChildren()){
                    for (DataSnapshot village : snap.child("Villages").getChildren()) {
                        if(name.equals(String.valueOf(village.getKey())))
                            if(village.child("Sights").exists())
                                for (DataSnapshot sight : village.child("Sights").getChildren()){

                                    String x = String.valueOf(sight.child("Location").child("X").getValue());
                                    String y = String.valueOf(sight.child("Location").child("Y").getValue());
                                    LatLng loc =new LatLng(Double.parseDouble(x),Double.parseDouble(y));
                                     sights.add(new Sight(String.valueOf(sight.getKey()),
                                            String.valueOf(sight.child("NameTrans").getValue()),
                                            String.valueOf(sight.child("Info").child("EN").getValue()),
                                            String.valueOf(sight.child("Info").child("GR").getValue()),
                                            loc,
                                            name));
                                }
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("fire_error",error.getMessage());
            }
        });
        return sights;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setInfo_en(String info_en) {
        this.info_en = info_en;
    }

    public void setInfo_gr(String info_gr) {
        this.info_gr = info_gr;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setVillage(String village) {
        this.village = village;
    }



    public String getName() {
        return name;
    }

    public String getInfo_en() {
        return info_en;
    }

    public String getInfo_gr() {
        return info_gr;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getVillage() {
        return village;
    }


}
