package cl.sidan.clac.fragments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cl.sidan.clac.R;

public class MyBeerAdapter extends ArrayAdapter<String> {
    LayoutInflater inflater;
    String[] objects;
    String[] subtext;

    public MyBeerAdapter(LayoutInflater inflater, Context context, int textViewResourceId, String[] objects, String[] subtext) {
        super(context, textViewResourceId, objects);
        this.inflater = inflater;
        this.objects = objects;
        this.subtext = subtext;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        View row = inflater.inflate(R.layout.beer_spinner_item, parent, false);
        TextView label = (TextView) row.findViewById(R.id.text_main_seen);
        TextView sublabel = (TextView) row.findViewById(R.id.sub_text_seen);
        label.setText(objects[position]);
        sublabel.setText(subtext[position]);

        ImageView icon = (ImageView)row.findViewById(R.id.icon);
        // icon.setImageResource(R.drawable.olsug_32);

        return row;
    }
}
