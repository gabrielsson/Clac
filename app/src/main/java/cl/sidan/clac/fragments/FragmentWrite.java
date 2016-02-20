package cl.sidan.clac.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

// import com.sileria.android.view.SlidingTray;
// import com.sileria.util.Side;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.R;

public class FragmentWrite extends Fragment {
    boolean hemlis = false;
    String text = "";
    private static final int FILE_SELECT_CODE = 0;

    private View rootView = null;

    private static FragmentWrite writeFragment;

    public static FragmentWrite newInstance() {
        if( null == writeFragment ) {
            writeFragment = new FragmentWrite();
        }
        return writeFragment;
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_write_entry, container, false);

        // SlidingTray drawer = (SlidingTray) inflater.inflate(R.layout.write_entry_drawer, null);
        //FrameLayout parent = (FrameLayout) rootView.findViewById(R.id.container_drawer);

        // drawer.setOrientation(SlidingTray.LEFT);
        // drawer.setHandlePosition(Side.BOTTOM);

        // parent.addView(drawer);

        //((CheckBox) rootView.findViewById(R.id.write_entry_secret)).setChecked(hemlis);
        //((TextView) rootView.findViewById(R.id.write_entry_text)).setText(text);
        rootView.findViewById(R.id.write_entry_text).requestFocus();

        final Spinner beers = (Spinner) rootView.findViewById(R.id.number_of_beers);
        String[] bira = {"0", "1", "2", "3", "4", "5", "??"};
        String[] biraSubtext = {"Inga öl", "Helan", "Halvan", "Tersen", "Kvarten", "Kvinten", "Kreaturens (åter)uppståndelse"};
        beers.setAdapter(new MyBeerAdapter(inflater, getActivity(), R.layout.beer_spinner_item, bira, biraSubtext));

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
                if (numBeers == 6) {
                    entry.setEnheter(16);
                } else {
                    entry.setEnheter(numBeers);
                }

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

                //MenuItem positionSettingItem = menu.findItem(R.id.position_setting);

                entry.setMessage(text);
                entry.setSecret(hemlis);

                boolean reportPosition = ((MainActivity) getActivity()).getPrefs().getBoolean("positionSetting", false);
                Location myLocation = ((MainActivity) getActivity()).getLocation();
                if( reportPosition && myLocation != null ) {
                    entry.setLatitude(BigDecimal.valueOf(myLocation.getLatitude()));
                    entry.setLongitude(BigDecimal.valueOf(myLocation.getLongitude()));
                }

                // Vi sätter kumpaner i MainActivity också
                ((MainActivity) getActivity()).createEntryAndSend(entry);
                //((MainActivity) getActivity()).setCurrentItem(MyFragmentPagerAdapter.FragmentOrder.readentry);
                getActivity().getSupportFragmentManager().popBackStack();

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

        TextView tv = (TextView) rootView.findViewById(R.id.write_entry_text);
        tv.setTextSize(font_size);
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
}

