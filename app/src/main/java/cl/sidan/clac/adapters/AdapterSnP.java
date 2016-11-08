package cl.sidan.clac.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.SnP;

public class AdapterSnP extends ArrayAdapter<SnP> {

    private Context context;
    private int layout;
    private float fontsize = 15;

    public AdapterSnP(Context context, int resource, List<SnP> objects, float fontsize) {
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
        TextView txtEmail = null;
        TextView txtHistory = null;
    }

    @Override
    public final SnP getItem(int position) {
        if (position < getCount()) {
            return super.getItem(position);
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        SnP user = getItem(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(layout, null);

            holder.memberHolder = (RelativeLayout) convertView.findViewById(R.id.snpHolder);
            holder.txtSignature = (TextView) convertView.findViewById(R.id.snp_signatur);
            holder.txtName = (TextView) convertView.findViewById(R.id.snp_namn);
            holder.txtEmail = (TextView) convertView.findViewById(R.id.snp_mail);
            holder.txtPhone = (TextView) convertView.findViewById(R.id.snp_tele);
            holder.txtHistory = (TextView) convertView.findViewById(R.id.snp_hist);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        convertView.setTag(holder);

        if (user == null) {
            return convertView;
        }

        if ( holder.memberHolder != null ) {
            holder.memberHolder.setBackgroundResource(R.drawable.background);
        }

        if ( holder.txtSignature != null ) {
            holder.txtSignature.setText(user.getSignature());
            holder.txtSignature.setTextSize(fontsize);
            holder.txtSignature.setBackgroundResource(R.drawable.background);
        }
        if ( holder.txtName != null ) {
            holder.txtName.setText(user.getName());
            holder.txtName.setTextSize(fontsize);
        }
        if ( holder.txtPhone != null ) {
            holder.txtPhone.setText(user.getPhone());
            holder.txtPhone.setTextSize(fontsize);
        }
        if ( holder.txtEmail != null ) {
            holder.txtEmail.setText(user.getEmail());
            holder.txtEmail.setTextSize(fontsize);
        }
        if ( holder.txtHistory != null ) {
            holder.txtHistory.setText(user.getHistory());
            holder.txtHistory.setTextSize(fontsize);
        }

        return convertView;
    }

}
