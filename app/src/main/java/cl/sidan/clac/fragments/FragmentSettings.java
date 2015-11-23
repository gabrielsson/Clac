package cl.sidan.clac.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import cl.sidan.clac.R;
import cl.sidan.clac.interfaces.GCMChangeListener;
// import cl.sidan.util.GCMUtil;

public class FragmentSettings extends Fragment implements GCMChangeListener {
    private static final float MULTIPLIER = 10;
    private static final float DEFAULT_FONTSIZE = 15;

    private TextView fontsize_text = null;
    private CheckBox cbNotifications = null;
    private CheckBox cbPosition = null;

    private static FragmentSettings settingsFragment;

    public static FragmentSettings newInstance() {
        if( null == settingsFragment ) {
            settingsFragment = new FragmentSettings();
        }
        return settingsFragment;
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        SeekBar volumeControl = (SeekBar) rootView.findViewById(R.id.fontsize);
        fontsize_text = (TextView) rootView.findViewById(R.id.fontsize_text);
        final SharedPreferences preferences = ((MainActivity) getActivity()).getPrefs();

        int default_size = (int) preferences.getFloat("font_size", DEFAULT_FONTSIZE);
        fontsize_text.setTextSize(default_size);
        volumeControl.setProgress((int) (default_size * MULTIPLIER));

        volumeControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            float progressChanged = 0;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = (float) progress / MULTIPLIER;
                preferences.edit().putFloat("font_size", progressChanged).apply();
                fontsize_text.setTextSize(progressChanged);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        cbPosition = (CheckBox) rootView.findViewById(R.id.position_setting);
        boolean positionSetting = preferences.getBoolean("positionSetting", true);
        Log.d("Location", "Location setting is " + positionSetting);
        cbPosition.setChecked(positionSetting);
        cbPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = cbPosition.isChecked();
                preferences.edit().putBoolean("positionSetting", checked).apply();
                Log.d("Location", "Location changed to " + checked);
                ((MainActivity) getActivity()).notifyLocationChange();
            }
        });

        cbNotifications = (CheckBox) rootView.findViewById(R.id.notifications);
        boolean notifications = preferences.getBoolean("notifications", false);
        cbNotifications.setChecked(notifications);
        cbNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean checked = !cbNotifications.isChecked();
                preferences.edit().putBoolean("notifications", checked).apply();
                ((MainActivity) getActivity()).notifyGCMChange();

                updateNotifications();
            }
        });
        updateNotifications();

        return rootView;
    }

    public final void updateNotifications() {
        SharedPreferences sp = ((MainActivity) getActivity()).getPrefs();
        if( true ) { // GCMUtil.isRegistered(getActivity()) ) {
            sp.edit().putBoolean("notifications", true).apply();
        } else {
            sp.edit().putBoolean("notifications", false).apply();
        }
    }

    @Override
    public final void onGCMChange(boolean success, String msg) {
        updateNotifications();
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
}
