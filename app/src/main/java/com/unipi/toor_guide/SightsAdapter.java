package com.unipi.toor_guide;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

public class SightsAdapter extends ArrayAdapter<Sight> {
    private Context context;

    private int res;

    public SightsAdapter(Context contxt, int resources, List<Sight> objects){
        super(contxt,resources,objects);
        context = contxt;
        res = resources;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String sight_name = getItem(position).getName();
        String sight_grname = getItem(position).getGr_name();
        String sight_info_en = getItem(position).getInfo_en();
        String sight_info_gr = getItem(position).getInfo_gr();
        LatLng sight_loc = getItem(position).getLocation();
        String sight_village = getItem(position).getVillage();
        Sight sight = new Sight(sight_name,sight_grname,sight_info_en,sight_info_gr,sight_loc,sight_village);

        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(res,parent,false);

        TextView txt_name = convertView.findViewById(R.id.sight_name);
        TextView txt_info = convertView.findViewById(R.id.sight_info);

        String[] systemLangs = Resources.getSystem().getConfiguration().getLocales().toLanguageTags().split(",");

        if (systemLangs[0].contains(Locale.forLanguageTag("EL").toLanguageTag())){
            txt_name.setText(sight_grname);
            txt_info.setText(sight_info_gr);
        }else{
            txt_name.setText(sight_name);
            txt_info.setText(sight_info_en);
        }
        txt_info.setMovementMethod(new ScrollingMovementMethod());
        ImageButton findonmap = convertView.findViewById(R.id.findonMap);

        findonmap.setOnClickListener(v -> context.startActivity(new Intent(context,MapsActivity.class)
                .putExtra("name",sight_name).putExtra("loc",sight_loc).putExtra("grname",sight_grname)));

        return convertView;
    }


}
