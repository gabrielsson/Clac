package cl.sidan.clac.fragments;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.Arr;
import cl.sidan.clac.access.interfaces.User;

/**
 * Created by Christofer on 2016-02-20.
 */
public class AdapterMembers extends ArrayAdapter<User> {

    private Context context;
    private float fontsize = 15;

    public AdapterMembers(Context context, int resource, List<User> objects, float fontsize) {
        super(context, resource, objects);
        this.context = context;
        this.fontsize = fontsize;
    }

    private static class ViewHolder {
        TextView txtSignature = null;
        TextView txtName = null;
        TextView txtPhone = null;
        //TextView txtIm = null;
        TextView txtTitle = null;

    }

    @Override
    public final User getItem(int position) {
        if (position < getCount()) {
            return super.getItem(position);
        }
        return null;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        User user = getItem(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.fragment_member, null);

            holder.txtSignature = (TextView) convertView.findViewById(R.id.memberSignature);
            holder.txtName = (TextView) convertView.findViewById(R.id.memberName);
            holder.txtPhone = (TextView) convertView.findViewById(R.id.memberNumber);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.memberTitle);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        convertView.setTag(holder);

        if (user == null) {
            return convertView;
        }
        /**
        holder.txtSignature.setText("Derp");
        holder.txtName.setText("Namn");
        holder.txtPhone.setText("Phone");
        holder.txtTitle.setText("Title");
        */
        holder.txtSignature.setText(user.getSignature());
        holder.txtName.setText(user.getName());
        holder.txtPhone.setText(user.getPhone());
        holder.txtTitle.setText(user.getTitle());


        holder.txtSignature.setTextSize(fontsize);
        holder.txtName.setTextSize(fontsize);
        holder.txtPhone.setTextSize(fontsize);
        holder.txtTitle.setTextSize(fontsize);

        return convertView;
    }

}


