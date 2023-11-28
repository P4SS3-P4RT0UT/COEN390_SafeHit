package com.example.coen390_safehit.controller;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.coen390_safehit.R;
import com.example.coen390_safehit.model.Colors;
import com.example.coen390_safehit.model.Player;

import java.util.List;
import java.util.Random;

public class PlayerAdapter extends ArrayAdapter<Player> {
    public PlayerAdapter(Context context, List<Player> players) {
        super(context, 0, players);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_list_view, parent, false);
        }
        Player player = getItem(position);

        TextView itemName = convertView.findViewById(R.id.itemName);
        TextView itemDetail = convertView.findViewById(R.id.itemDetail);
        TextView initals = convertView.findViewById(R.id.initials);

        if (player != null) {
            itemName.setText(player.getFirstName() + " " + player.getLastName());
            itemDetail.setText(player.getPosition() + " #" + player.getNumber());
            initals.setText(player.getFirstName().substring(0, 1).toUpperCase() + player.getLastName().substring(0, 1).toUpperCase());
            Random rnd = new Random();
            initals.setBackgroundTintList(ColorStateList.valueOf(Colors.getRandomColor()));
//            initals.setBackgroundColor(Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
        }

        return convertView;
    }

}
