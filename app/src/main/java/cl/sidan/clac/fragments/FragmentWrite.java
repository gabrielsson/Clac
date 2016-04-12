package cl.sidan.clac.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class FragmentWrite extends Fragment {
    boolean hemlis = false;
    private static final int FILE_SELECT_CODE = 0;

    private View rootView = null;

    private ArrayList<User> kumpaner = new ArrayList<>();
    private ArrayList<User> selectedKumpaner = new ArrayList<>();
    private ArrayList<Entry> notSentList = new ArrayList<>();

    private AdapterMembers memberAdapter;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_write_entry, container, false);

        rootView.findViewById(R.id.write_entry_text).requestFocus();

        final Spinner beers = (Spinner) rootView.findViewById(R.id.number_of_beers);
        String[] bira = {"0", "1", "2", "3", "4", "5"};
        String[] biraSubtext = {"Inga öl", "Helan", "Halvan", "Tersen", "Kvarten", "Kvinten"};
        beers.setAdapter(new AdapterBeer(inflater, getActivity(), R.layout.beer_spinner_item, bira, biraSubtext));

        ImageView choose_kumpaner = (ImageView) rootView.findViewById(R.id.choose_kumpaner);
        choose_kumpaner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRapporteraKumpanerPopUp();
            }
        });

        float font_size = ((MainActivity) getActivity()).getPrefs().getFloat("font_size", 15);

        if(memberAdapter == null) {
            memberAdapter = new AdapterMembers(getActivity(), R.layout.adapter_kumpaner_item, kumpaner, font_size);
            memberAdapter.setNotifyOnChange(true);
        }

        TextView kumpanText = (TextView) rootView.findViewById(R.id.kumpaner);
        String kumpanString = "";
        for (User u : selectedKumpaner) {
            kumpanString += u.getSignature() + ", ";
        }
        kumpanText.setText(kumpanString);

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
                    Toast.makeText(getActivity(), "Please install a File Manager.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });*/

        rootView.findViewById(R.id.write_entry_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(getClass().getCanonicalName(), "writeEntrySend.onClick()");

                EditText entryText = (EditText) getActivity().findViewById(R.id.write_entry_text);
                Spinner entryBeers = (Spinner) getActivity().findViewById(R.id.number_of_beers);
                String text = entryText.getText().toString();
                RequestEntry entry = new RequestEntry();

                int numBeers = entryBeers.getSelectedItemPosition();
                Log.d(getClass().getCanonicalName(), "Reporting " + numBeers + " beers");
                entry.setEnheter(numBeers);

                entry.setKumpaner(selectedKumpaner);

                /* Ladda upp bild */
                ImageView imageView = (ImageView) getActivity().findViewById(R.id.write_entry_imagechoosen);
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

                if (text.trim().isEmpty() && base64Image == null && numBeers == 0) {
                    Toast.makeText(view.getContext(), getString(R.string.write_entry_not_empty), Toast.LENGTH_SHORT).show();
                    return;
                }

                entry.setMessage(text);
                entry.setSecret(hemlis);

                boolean reportPosition = ((MainActivity) getActivity()).getPrefs().getBoolean("positionSetting", true);
                Location myLocation = ((MainActivity) getActivity()).getLocation();
                if (reportPosition && myLocation != null) {
                    entry.setLatitude(BigDecimal.valueOf(myLocation.getLatitude()));
                    entry.setLongitude(BigDecimal.valueOf(myLocation.getLongitude()));
                }

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
        });

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        float font_size = ((MainActivity) getActivity()).getPrefs().getFloat("font_size", 15);

        EditText tv = (EditText) rootView.findViewById(R.id.write_entry_text);
        tv.setTextSize(font_size);
        tv.requestFocus();

        if(memberAdapter == null) {
            Log.w(getTag(), "XXX_SWO: onResume: memberAdapter == null. If this row happen, then the this if statement is necessary.");
            memberAdapter = new AdapterMembers(getActivity(), R.layout.adapter_kumpaner_item, kumpaner, font_size);
            memberAdapter.setNotifyOnChange(true);
        }

        TextView kumpanText = (TextView) rootView.findViewById(R.id.kumpaner);
        String kumpanString = "";
        for (User u : selectedKumpaner) {
            kumpanString += u.getSignature() + ", ";
        }
        kumpanText.setText(kumpanString);

        new GetKumpanerAsync().execute();
    }

    @Override
    public final void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE && resultCode == MainActivity.RESULT_OK && data != null) {
            Uri uri = data.getData();

            ImageView imageView = (ImageView) getActivity().findViewById(R.id.write_entry_imagechoosen);

            try {
                String path = getPath(uri);

                imageView.setImageURI(uri);
                imageView.setTag(path);

                Log.d("IMAGEUPLOAD", "File selected: " + path);
                Toast.makeText(getActivity(), "File Selected: " + path, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e("IMAGEUPLOAD", "File select error", e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showRapporteraKumpanerPopUp() {
        final Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.popup_kumpaner);

        final GridView gv = (GridView) dialog.findViewById(R.id.kumpan_grid);
        final Button cancelButton = (Button) dialog.findViewById(R.id.kumpan_cancel);
        final Button okButton = (Button) dialog.findViewById(R.id.kumpan_sup);

        gv.setAdapter(memberAdapter);
        memberAdapter.setSelected(selectedKumpaner);

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (!selectedKumpaner.contains(kumpaner.get(position))) {
                    v.setSelected(true);
                    selectedKumpaner.add(memberAdapter.getItem(position));
                } else {
                    v.setSelected(false);
                    selectedKumpaner.remove(memberAdapter.getItem(position));
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        memberAdapter.notifyDataSetChanged();
                    }
                });
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
                String kumpanString = "";
                for (User u : selectedKumpaner) {
                    kumpanString += u.getSignature() + ", ";
                }
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
                if( onCreateEntry(e) ) {
                    notSentList.remove(i);
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

            boolean isSuccess = ((MainActivity) getActivity()).sidanAccess().createEntry(
                    entry.getMessage(), entry.getLatitude(), entry.getLongitude(),
                    entry.getEnheter(), entry.getStatus(), host, entry.getSecret(), entry.getImage(),
                    entry.getFileName(), entry.getKumpaner());

            if( isSuccess ) {
                Log.d("WriteEntry", "Successfully created entry, now notifying GCM users...");
                // GCMUtil.notifyGCM(getApplicationContext(), number, entry.getMessage());
            }

            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean retur) {
            if (!retur) {
                Log.e("WriteEntry", "Could not create some Entries.");
            }
        }
    }
}

