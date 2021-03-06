package cl.sidan.clac.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.Entry;
import cl.sidan.clac.access.interfaces.User;
import cl.sidan.clac.adapters.AdapterBeer;
import cl.sidan.clac.adapters.AdapterMembers;
import cl.sidan.clac.objects.RequestEntry;
import cl.sidan.clac.objects.RequestUser;

public class FragmentWrite extends Fragment implements OnMapReadyCallback, LocationListener {
    private static final int FILE_SELECT_CODE = 0;

    private View rootView = null;
    private SharedPreferences preferences;

    private ArrayList<User> kumpaner = new ArrayList<>();
    private ArrayList<String> selectedKumpaner = new ArrayList<>();
    private ArrayList<Entry> notSentList = new ArrayList<>();

    private AdapterMembers memberAdapter;
    private AdapterBeer beerAdapter;

    public static MapView mapView;
    private boolean myPositionDisabled = true;

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_write_entry, container, false);
        preferences = ((MainActivity) getActivity()).getPrefs();

        final Spinner beers = (Spinner) rootView.findViewById(R.id.number_of_beers);
        String[] antalEnheter = getResources().getStringArray(R.array.antalEnheterInklNoll),
                namnEnheter = getResources().getStringArray(R.array.namnEnheterInklNoll);
        beerAdapter = new AdapterBeer(inflater, getActivity(), R.layout.beer_spinner_item, antalEnheter, namnEnheter);
        beers.setAdapter(beerAdapter);

        ImageView choose_kumpaner = (ImageView) rootView.findViewById(R.id.choose_kumpaner);
        choose_kumpaner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRapporteraKumpanerPopUp();
            }
        });

        float font_size = preferences.getFloat("font_size", 15);

        memberAdapter = new AdapterMembers(getActivity(), R.layout.adapter_kumpaner_item, kumpaner, font_size);
        memberAdapter.setNotifyOnChange(true);

        TextView kumpanText = (TextView) rootView.findViewById(R.id.kumpaner);
        String kumpanString = TextUtils.join(",", selectedKumpaner);
        kumpanText.setText(kumpanString);

        ((MainActivity) getActivity()).addLocationListener(this);

        mapView = (MapView) rootView.findViewById(R.id.write_entry_position);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

       /* rootView.findViewById(R.id.bilduppladdning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(
                            Intent.createChooser(intent, "Select a File to Upload"),
                            FILE_SELECT_CODE);
                } catch (ActivityNotFoundException ex) {
                    // Potentially direct the user to the Market with a Dialog
                    Toast.makeText(rootView.getContext(), "Please install a File Manager.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });*/

        rootView.findViewById(R.id.write_entry_text).requestFocus();
        rootView.findViewById(R.id.write_entry_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(getClass().getCanonicalName(), "writeEntrySend.onClick()");

                EditText entryText = (EditText) rootView.findViewById(R.id.write_entry_text);
                Spinner entryBeers = (Spinner) rootView.findViewById(R.id.number_of_beers);
                CheckBox entrySecret = (CheckBox) rootView.findViewById(R.id.write_entry_secret);
                String text = entryText.getText().toString();
                RequestEntry entry = new RequestEntry();

                int numBeers = entryBeers.getSelectedItemPosition();
                boolean hemlis = entrySecret.isChecked();
                Log.d(getClass().getCanonicalName(), "Reporting " + numBeers + " beers");
                entry.setEnheter(numBeers);

                ArrayList<User> kumpaner = new ArrayList<>();
                for(String signature : selectedKumpaner) {
                    kumpaner.add(new RequestUser(signature));
                }
                entry.setKumpaner(kumpaner);

                /* Ladda upp bild */
                ImageView imageView = (ImageView) rootView.findViewById(R.id.write_entry_imagechoosen);
                String base64Image = null;
                if (imageView.getDrawable() != null) {
                    String path = (String) imageView.getTag();

                    Bitmap selectedImage = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    base64Image = Base64.encodeToString(byteArray, Base64.URL_SAFE | Base64.NO_WRAP);

                    entry.setImage(base64Image);
                    entry.setFileName(Base64.encodeToString(path.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP));
                    Log.d("IMAGEDRAWER", "Image will be uploaded with name " + path);
                    Log.d("IMAGEDRAWER", "Base64Image length: " + base64Image.length());
                }

                entry.setMessage(text);
                entry.setSecret(hemlis);
                // Is this a good idea?
                // Pros: if we cannot send the entry right away, and try a new resending later,
                // the entry will still have the correct date/time.
                // Cons: time in the server vs the device is probably different and might cause
                // messages to get out of sync.
                // entry.setDateTime(now);

                boolean reportPosition = preferences.getBoolean("positionSetting", true);
                Location myLocation = ((MainActivity) getActivity()).getLocation();
                if (reportPosition && myLocation != null) {
                    LatLng pos = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    entry.setLatitude(BigDecimal.valueOf(pos.latitude));
                    entry.setLongitude(BigDecimal.valueOf(pos.longitude));
                }

                if (text.trim().isEmpty() && base64Image == null && numBeers == 0) {
                    Toast.makeText(view.getContext(), getString(R.string.write_entry_not_empty), Toast.LENGTH_SHORT).show();
                } else {
                    new CreateEntryAsync().execute(entry);

                    // en Entry är skapat, resetta allt.
                    entryText.setText("");
                    imageView.setTag("");
                    imageView.setImageDrawable(null);
                    entryBeers.setSelection(0);

                    // Stäng meny
                    DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.END);
                }
            }
        });

        EditText tv = (EditText) rootView.findViewById(R.id.write_entry_text);
        tv.setTextSize(font_size);
        tv.requestFocus();

        if (null != savedInstanceState) {
            tv.setText(savedInstanceState.getString("EntryText"));
            selectedKumpaner = savedInstanceState.getStringArrayList("SelectedNumbers");
            kumpanString = TextUtils.join(",", selectedKumpaner);
            kumpanText.setText(kumpanString);

            Spinner entryBeers = (Spinner) rootView.findViewById(R.id.number_of_beers);
            entryBeers.setSelection(savedInstanceState.getInt("EntryBeers"));

            CheckBox entrySecret = (CheckBox) rootView.findViewById(R.id.write_entry_secret);
            entrySecret.setChecked(savedInstanceState.getBoolean("EntrySecret"));
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetKumpanerAsync().execute();
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        Spinner entryBeers = (Spinner) rootView.findViewById(R.id.number_of_beers);
        CheckBox entrySecret = (CheckBox) rootView.findViewById(R.id.write_entry_secret);
        EditText tv = (EditText) rootView.findViewById(R.id.write_entry_text);

        state.putStringArrayList("SelectedNumbers", selectedKumpaner);
        state.putString("EntryText", tv.getText().toString());
        state.putBoolean("EntrySecret", entrySecret.isChecked());
        state.putInt("EntryBeers", entryBeers.getSelectedItemPosition());
    }

    @Override
    public final void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE && resultCode == MainActivity.RESULT_OK && data != null) {
            Uri uri = data.getData();

            ImageView imageView = (ImageView) rootView.findViewById(R.id.write_entry_imagechoosen);

            try {
                String path = getPath(uri);

                imageView.setImageURI(uri);
                imageView.setTag(path);

                Log.d("IMAGEUPLOAD", "File selected: " + path);
                Toast.makeText(rootView.getContext(), "File Selected: " + path, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e("IMAGEUPLOAD", "File select error", e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        boolean reportPosition = preferences.getBoolean("positionSetting", true);
        MainActivity mainActivity = ((MainActivity) getActivity());
        Location myLoc = mainActivity != null ? mainActivity.getLocation() : null;

        if (reportPosition && myLoc != null) {
            LatLng pos = new LatLng(myLoc.getLatitude(), myLoc.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f));
            map.setMyLocationEnabled(true);

            rootView.findViewById(R.id.write_entry_position).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.write_entry_position_alt_text).setVisibility(View.GONE);
        } else {
            rootView.findViewById(R.id.write_entry_position).setVisibility(View.GONE);
            rootView.findViewById(R.id.write_entry_position_alt_text).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Write - Location", "Got location change " + location);
        mapView.getMapAsync(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Write - Location", "Got status change for provider " + provider + " - " + status);
        mapView.getMapAsync(this);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Write - Location", "Provider " + provider + " enabled");
        rootView.findViewById(R.id.write_entry_position).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.write_entry_position_alt_text).setVisibility(View.GONE);
        mapView.getMapAsync(this);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Write - Location", "Provider " + provider + " disabled");
        //rootView.findViewById(R.id.write_entry_position).setVisibility(View.GONE);
        //rootView.findViewById(R.id.write_entry_position_alt_text).setVisibility(View.VISIBLE);
        mapView.getMapAsync(this);
    }

    private void showRapporteraKumpanerPopUp() {
        final Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.popup_kumpaner);

        final GridView gv = (GridView) dialog.findViewById(R.id.kumpan_grid);
        final Button cancelButton = (Button) dialog.findViewById(R.id.kumpan_cancel);
        final Button okButton = (Button) dialog.findViewById(R.id.kumpan_sup);

        if (null == gv.getAdapter()) {
            gv.setAdapter(memberAdapter);
        }

        memberAdapter.setSelected(selectedKumpaner);
        memberAdapter.notifyDataSetChanged();

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (!selectedKumpaner.contains(kumpaner.get(position).getSignature())) {
                    v.setSelected(true);
                    selectedKumpaner.add(memberAdapter.getItem(position).getSignature());
                } else {
                    v.setSelected(false);
                    selectedKumpaner.remove(memberAdapter.getItem(position).getSignature());
                }
                Activity activity = getActivity();
                if (null != activity) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            memberAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });

        dialog.setCancelable(true);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedKumpaner.clear();
                TextView kumpanText = (TextView) rootView.findViewById(R.id.kumpaner);
                kumpanText.setText("");
                dialog.cancel();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView kumpanText = (TextView) rootView.findViewById(R.id.kumpaner);
                String kumpanString = TextUtils.join(",", selectedKumpaner);
                kumpanText.setText(kumpanString);
                dialog.hide();
            }
        });

        dialog.show();
    }

    private final class GetKumpanerAsync extends AsyncTask<Void, Void, List<User>> {
        @Override
        protected List<User> doInBackground(Void... voids) {
            return ((MainActivity) getActivity()).sidanAccess().readMembers(true);
        }

        @Override
        protected void onPostExecute(List<User> tempKumpaner) {
            if ( !tempKumpaner.isEmpty() ) {
                Collections.reverse(tempKumpaner);
                kumpaner.clear();
                kumpaner.addAll(tempKumpaner);
                memberAdapter.notifyDataSetChanged();
                Log.d("Kumpaner", "New Kumpaner! Yay!");
            } else {
                Log.e("Kumpaner", "Could not get new kumpaner");
            }
        }
    }

    public String getPath(Uri uri) {
        Cursor returnCursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        int nameIndex;
        String path = "";
        if (returnCursor != null) { // maybe throw an exception if this is not true
            nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            path = returnCursor.getString(nameIndex);
            returnCursor.close();
        }

        return path;
    }

    public final class CreateEntryAsync extends AsyncTask<Entry, Entry, Boolean> {
        @Override
        protected Boolean doInBackground(Entry... entries) {
            Collections.addAll(notSentList, entries);

            Entry e;
            for(int i = 0; i < notSentList.size(); i++) {
                e = notSentList.get(i);

                if (e.getMessage().trim().isEmpty() && e.getImage() == null && e.getEnheter() == 0) {
                    notSentList.remove(i);
                    i--;
                } else if (onCreateEntry(e)) {
                    notSentList.remove(i);
                    i--;
                }
            }
            return notSentList.isEmpty();
        }


        public final boolean onCreateEntry(Entry entry) {
            String host = null;
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            return ((MainActivity) getActivity()).sidanAccess().createEntry(
                    entry.getMessage(), entry.getLatitude(), entry.getLongitude(),
                    entry.getEnheter(), entry.getStatus(), host, entry.getSecret(), entry.getImage(),
                    entry.getFileName(), entry.getKumpaner());
        }

        @Override
        protected void onPostExecute(Boolean retur) {
            if (!retur) {
                Log.e("WriteEntry", "Could not create some Entries.");
                Toast.makeText(rootView.getContext(),
                        "Kunde inte posta alla meddelanden just nu, försök senare.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}

