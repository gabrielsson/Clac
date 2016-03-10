package cl.sidan.clac.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.User;

public class AdapterMembers extends ArrayAdapter<User> {

    private Context context;
    private int layout;
    private float fontsize = 15;
    private List<User> selectedObjects = new ArrayList<>();
    private HashSet<String> ignoredMembers = new HashSet<>();

    public AdapterMembers(Context context, int resource, List<User> objects, float fontsize) {
        super(context, resource, objects);
        this.context = context;
        this.fontsize = fontsize;
        this.layout = resource;
    }

    private static class ViewHolder {
        RelativeLayout memberHolder = null;

        TextView txtSignature = null;
        TextView txtName = null;
        TextView txtPhone = null;
        //TextView txtIm = null;
        TextView txtTitle = null;
    }

    public void setSelected(List<User> objects) {
        selectedObjects = objects;
    }

    public void setIgnoredMembers(HashSet<String> ignoredMembers) {
        this.ignoredMembers = ignoredMembers;
    }

    @Override
    public final User getItem(int position) {
        if (position < getCount()) {
            return super.getItem(position);
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        User user = getItem(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(layout, null);

            holder.memberHolder = (RelativeLayout) convertView.findViewById(R.id.memberHolder);
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

        if ( holder.memberHolder != null ) {
            holder.memberHolder.setBackgroundResource(R.drawable.background);
            if ( ignoredMembers.contains(user.getSignature()) ) {
                holder.memberHolder.setBackgroundResource(R.drawable.list_selector_ignored);
            }
        }

        if ( holder.txtSignature != null ) {
            holder.txtSignature.setText(user.getSignature());
            holder.txtSignature.setTextSize(fontsize);

            holder.txtSignature.setBackgroundResource(R.drawable.background);
            if ( selectedObjects.contains(user) || ignoredMembers.contains(user.getSignature()) ) {
                holder.txtSignature.setBackgroundResource(R.drawable.list_selector_ignored);
            }
        }
        if ( holder.txtName != null ) {
            holder.txtName.setText(user.getName());
            holder.txtName.setTextSize(fontsize);
        }
        if ( holder.txtPhone != null ) {
            holder.txtPhone.setText(user.getPhone());
            holder.txtPhone.setTextSize(fontsize);
        }
        if ( holder.txtTitle != null ) {
            holder.txtTitle.setText(user.getTitle());
            holder.txtTitle.setTextSize(fontsize);
        }

        return convertView;
    }

}


