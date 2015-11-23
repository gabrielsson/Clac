package cl.sidan.clac.access.fragments;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cl.sidan.access.interfaces.Arr;

public class AdapterArr extends ArrayAdapter<Arr> {
    private Context context;
    private float fontsize = 15;

    public AdapterArr(Context context, int resource, List<Arr> objects, float fontsize) {
        super(context, resource, objects);
        this.context = context;
        this.fontsize = fontsize;
    }

    private static class ViewHolder {
        TextView txtArrNamn = null;
        TextView txtArrDatum = null;
        TextView txtArrDeltagare = null;
        TextView txtArrHetsade = null;
    }

    @Override
    public final Arr getItem(int position) {
        if (position < getCount()) {
            return super.getItem(position);
        }
        return null;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Arr arr = getItem(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.arr, null);
            holder.txtArrNamn = (TextView) convertView.findViewById(R.id.arr_namn);
            holder.txtArrDatum = (TextView) convertView.findViewById(R.id.arr_datum);
            holder.txtArrDeltagare = (TextView) convertView.findViewById(R.id.arr_deltagare);
            holder.txtArrHetsade = (TextView) convertView.findViewById(R.id.arr_hetsade);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        convertView.setTag(holder);

        if (arr == null) {
            return convertView;
        }

        String arrNamn = arr.getNamn();
        String arrPlats = arr.getPlats();
        String arrHetsade = arr.getHetsade();
        String arrKanske = arr.getKanske();
        String arrText = (arrNamn.isEmpty() ? "" : arrNamn) +
                (arrNamn.isEmpty() || arrPlats.isEmpty() ? "" : " @ ") +
                (arrPlats.isEmpty() ? "" : arrPlats);
        String arrHets = (arrHetsade.isEmpty() ? "" : " | Hets: " + arrHetsade) +
                (arrKanske.isEmpty() ? "" : " | Kanske: " + arrKanske);
        holder.txtArrNamn.setText(arrText);
        holder.txtArrDatum.setText(arr.getDatum());
        holder.txtArrDeltagare.setText(arr.getDeltagare());
        holder.txtArrHetsade.setText(arrHets);

        holder.txtArrNamn.setTextSize(fontsize);
        holder.txtArrDatum.setTextSize(fontsize);
        holder.txtArrDeltagare.setTextSize(fontsize);
        holder.txtArrHetsade.setTextSize(fontsize);

        return convertView;
    }

}
