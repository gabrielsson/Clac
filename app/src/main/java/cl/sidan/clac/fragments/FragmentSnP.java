package cl.sidan.clac.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.SnP;
import cl.sidan.clac.adapters.AdapterSnP;
import cl.sidan.clac.objects.RequestSnP;


public class FragmentSnP extends Fragment {
    private int scrollPosition = 0, top = 0;
    private View rootView;

    private ListView snpListView;

    private ArrayList<SnP> snplista = new ArrayList<>();
    private AdapterSnP snpAdapter = null;
    private SwipeRefreshLayout snpContainer = null;

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_snp, container, false);
        SharedPreferences preferences = ((MainActivity) getActivity()).getPrefs();
        float font_size = preferences.getFloat("font_size", 15);

        Button new_snp = (Button) rootView.findViewById(R.id.add_snp);
        new_snp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSkapaEllerUppdateraSnP(null);
            }
        });

        snpAdapter = new AdapterSnP(inflater.getContext(), R.layout.adapter_snp_item, snplista, font_size);
        snpAdapter.setNotifyOnChange(true);

        snpContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.snpContainer);
        snpContainer.setRefreshing(true);
        snpContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new GetSuspectsAndProspects().execute();
                    }
                }
        );

        snpListView = (ListView) rootView.findViewById(R.id.snp_lista);
        snpListView.setAdapter(snpAdapter);
        registerForContextMenu(snpListView);

        new GetSuspectsAndProspects().execute();

        if( null != savedInstanceState ) {
            scrollPosition = savedInstanceState.getInt("scrollPosition", 0);
            top = savedInstanceState.getInt("top", 0);
        }

        return rootView;
    }


    @Override
    public void onSaveInstanceState(Bundle state) {
        // Save scroll position
        scrollPosition = snpListView.getFirstVisiblePosition();
        top = getTop();

        state.putInt("scrollPosition", scrollPosition);
        state.putInt("top", top);

        super.onSaveInstanceState(state);
    }

    @Override
    public final void onCreateContextMenu(ContextMenu menu, View v,
                                          ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_snp, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        SnP user = snpAdapter.getItem(info.position);

        if ( user == null ) {
            return;
        }

        MenuItem callItem = menu.findItem(R.id.snp_ring);
        MenuItem smsItem = menu.findItem(R.id.snp_smsa);
        MenuItem mailItem = menu.findItem(R.id.snp_maila);

        if ( null == user.getPhone() || user.getPhone().isEmpty() ) {
            callItem.setEnabled(false);
            smsItem.setEnabled(false);
        }
        if ( null == user.getEmail() || user.getEmail().isEmpty() ) {
            mailItem.setEnabled(false);
        }
    }

    @Override
    public final boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Intent intent;
        SnP user = snpAdapter.getItem(info.position);
        if( user == null ) {
            return false;
        }

        switch (item.getItemId()) {

            case R.id.snp_ring:
                intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + user.getPhone()));
                startActivity(intent);
                return true;

            case R.id.snp_smsa:
                intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + user.getPhone()));
                startActivity(intent);
                return true;

            case R.id.snp_maila:
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ user.getEmail() });
                intent.putExtra(Intent.EXTRA_TEXT, "\n\n\nMvh\n" + ((MainActivity) getActivity()).getPrefs().getString("username", "#"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "");

                startActivity(intent);
                return true;

            case R.id.snp_uppdatera:
                showSkapaEllerUppdateraSnP(user);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    public int getTop() {
        View v = snpListView.getChildAt(0);
        if (null != v) {
            return (v.getTop() - snpListView.getPaddingTop());
        }
        return 0;
    }

    private void showSkapaEllerUppdateraSnP(SnP u) {
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(rootView.getContext());
        helpBuilder.setTitle(u != null ? "Uppdatera " + u.getSignature() : "Lägg till ny S/P");
        // helpBuilder.setIcon(R.drawable.olsug_32);

        LayoutInflater factory = LayoutInflater.from(rootView.getContext());
        final View textEntryView = factory.inflate(R.layout.popup_new_snp, null);
        helpBuilder.setView(textEntryView);
        final Spinner snpSorPSpinner = (Spinner) textEntryView.findViewById(R.id.snp_popup_s_or_p);
        final Spinner snpNummerSpinner = (Spinner) textEntryView.findViewById(R.id.snp_popup_nummer);
        final EditText snpNamnText = (EditText) textEntryView.findViewById(R.id.snp_popup_namn);
        final EditText snpEmailText = (EditText) textEntryView.findViewById(R.id.snp_popup_mail);
        final EditText snpPhoneText = (EditText) textEntryView.findViewById(R.id.snp_popup_telefon);
        final EditText snpHistoryText = (EditText) textEntryView.findViewById(R.id.snp_popup_history);

        ArrayList<Integer> numberArray = new ArrayList<>();
        for(int i=1; i<50; i++) { numberArray.add(i); }
        Integer num;
        for (SnP sorp : snplista) {
            num = sorp.getNumber();
            if( !numberArray.contains(num) ) {
                numberArray.add(num);
            }
        }

        ArrayAdapter<Integer> nummer_adapter = new ArrayAdapter<>(
                this.getContext(), android.R.layout.simple_spinner_item, numberArray);
        nummer_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        snpNummerSpinner.setAdapter(nummer_adapter);

        ArrayList<String> snpArray = new ArrayList<>();
        snpArray.add("S");
        snpArray.add("P");
        ArrayAdapter<String> snp_adapter = new ArrayAdapter<>(
                this.getContext(), android.R.layout.simple_spinner_item, snpArray);
        snp_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        snpSorPSpinner.setAdapter(snp_adapter);

        final Integer snpId = u == null ? -1 : u.getId();

        if( u != null ) {
            int indexNum = numberArray.indexOf(u.getNumber());
            int indexSorP = snpArray.indexOf(u.getStatus().toUpperCase());

            snpSorPSpinner.setSelection(indexSorP);
            snpNummerSpinner.setSelection(indexNum);
            snpNamnText.setText(u.getName());
            snpEmailText.setText(u.getEmail());
            snpPhoneText.setText(u.getPhone());
            snpHistoryText.setText(u.getHistory());
        }

        helpBuilder.setNegativeButton("Ångra",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                    }
                });
        helpBuilder.setPositiveButton(u != null ? "Uppdatera" : "Skapa",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String snpSorP = (String) snpSorPSpinner.getSelectedItem();
                        Integer snpNumber = (Integer) snpNummerSpinner.getSelectedItem();
                        String snpName = snpNamnText.getText().toString();
                        String snpEmail = snpEmailText.getText().toString();
                        String snpPhone = snpPhoneText.getText().toString();
                        String snpHist = snpHistoryText.getText().toString();

                        RequestSnP newSnP = new RequestSnP(snpSorP, snpNumber);
                        newSnP.setId(snpId);
                        newSnP.setName(snpName);
                        newSnP.setEmail(snpEmail);
                        newSnP.setPhone(snpPhone);
                        newSnP.setHistory(snpHist);

                        if( !snpName.isEmpty() ) {
                            new CreateOrUpdateSnPAsync().execute(newSnP);
                        }
                    }
                });

        // Remember, create doesn't show the dialog
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();
    }

    public final class CreateOrUpdateSnPAsync extends AsyncTask<SnP, Void, Boolean> {
        @Override
        protected Boolean doInBackground(SnP... snps) {
            SnP snp = snps[0];

            return ((MainActivity) getActivity()).sidanAccess().createOrUpdateSnP(
                    snp.getId(), snp.getStatus(), snp.getNumber(), snp.getName(),
                    snp.getEmail(), snp.getPhone(), snp.getHistory());
        }

        protected void onPostExecute(boolean success) {
            if (success) {
                Toast.makeText(rootView.getContext(),
                        "S eller P skapat!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(rootView.getContext(),
                        "Något gick snett. Försök senare!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public final class GetSuspectsAndProspects extends AsyncTask<String, Void, List<SnP>> {
        @Override
        protected List<SnP> doInBackground(String... strings) {
            return ((MainActivity) getActivity()).sidanAccess().readSnP();
        }

        @Override
        protected void onPostExecute(List<SnP> response) {
            if( null == response ) {
                Log.d(getClass().getCanonicalName(), "Response was empty from server, will not clear SnP.");
                Toast.makeText(rootView.getContext(),
                        "Kunde inte hämta Suspects och Prospects från servern.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Log.d(getClass().getCanonicalName(), "SnP from server: " + response.size());
                snplista.clear();
                snplista.addAll(response);
                snpAdapter.notifyDataSetChanged();
            }
            snpContainer.setRefreshing(false);
        }
    }
}
