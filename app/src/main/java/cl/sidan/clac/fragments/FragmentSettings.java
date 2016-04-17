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

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.R;

public class FragmentSettings extends Fragment {
    private static final float MULTIPLIER = 10;
    private static final float DEFAULT_FONTSIZE = 15;

    private TextView fontsize_text = null;
    private CheckBox cbPosition = null;

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

        TextView changePassword = (TextView) rootView.findViewById(R.id.change_password_link);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).getReusedFragment(new FragmentChangePassword());
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

        return rootView;
    }
}
