package cl.sidan.clac.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.Arr;
import cl.sidan.clac.access.interfaces.Poll;
import cl.sidan.clac.adapters.AdapterArr;
import cl.sidan.clac.objects.RequestArr;


public class FragmentArr extends Fragment {
    private List<Arr> arrlista = new ArrayList<>();
    private AdapterArr arrAdapter = null;
    private SwipeRefreshLayout arrContainer = null;
    private int lastPollId = 0;
    private ViewHolder holder = new ViewHolder();
    private Poll currentPoll = null;

    private View rootView;
    private SharedPreferences preferences;

    private static class ViewHolder {
        TextView txtQuestion = null;
        RadioButton radioAnswer1 = null;
        RadioButton radioAnswer2 = null;
        Button buttonVote = null;

        LinearLayout progressHolder1 = null;
        ProgressBar progressAnswer1 = null;
        TextView txtProgressAnswer1 = null;
        LinearLayout progressHolder2 = null;
        ProgressBar progressAnswer2 = null;
        TextView txtProgressAnswer2 = null;
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_arr, container, false);

        preferences = ((MainActivity) getActivity()).getPrefs();
        float font_size = preferences.getFloat("font_size", 15);

        Button new_arr = (Button) rootView.findViewById(R.id.add_arr);
        new_arr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSkapaEllerUppdateraArr(null);
            }
        });

        arrAdapter = new AdapterArr(inflater.getContext(), R.layout.arr, arrlista, font_size);
        arrAdapter.setNotifyOnChange(true);

        arrContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.arrContainer);
        arrContainer.setRefreshing(true);

        arrContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new ReadArrAsync().execute();
                        new ReadPollAsync().execute();
                    }
                }
        );

        ListView arr_lista = (ListView) rootView.findViewById(R.id.arr_lista);
        arr_lista.setAdapter(arrAdapter);
        registerForContextMenu(arr_lista);

        holder.txtQuestion = (TextView) rootView.findViewById(R.id.poll_question_text);
        holder.radioAnswer1 = (RadioButton) rootView.findViewById(R.id.poll_answer_1);
        holder.radioAnswer2 = (RadioButton) rootView.findViewById(R.id.poll_answer_2);
        holder.buttonVote = (Button) rootView.findViewById(R.id.poll_vote_button);

        holder.progressHolder1 = (LinearLayout) rootView.findViewById(R.id.poll_answer_1_progress_holder);
        holder.progressHolder2 = (LinearLayout) rootView.findViewById(R.id.poll_answer_2_progress_holder);
        holder.progressAnswer1 = (ProgressBar) rootView.findViewById(R.id.poll_answer_1_progress);
        holder.progressAnswer2 = (ProgressBar) rootView.findViewById(R.id.poll_answer_2_progress);
        holder.txtProgressAnswer1 = (TextView) rootView.findViewById(R.id.poll_answer_1_progress_text);
        holder.txtProgressAnswer2 = (TextView) rootView.findViewById(R.id.poll_answer_2_progress_text);

        holder.buttonVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int votedOn = ((RadioGroup) rootView.findViewById(R.id.poll_answer_group)).getCheckedRadioButtonId();
                if( votedOn > 0 ) {
                    new VotePollAsync().execute(votedOn == R.id.poll_answer_1 ? 1 : 0);
                }
            }
        });

        new ReadArrAsync().execute();
        new ReadPollAsync().execute();

        return rootView;
    }

    @Override
    public final void onCreateContextMenu(ContextMenu menu, View v,
                                          ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_arr, menu);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Arr arr = arrAdapter.getItem(info.position);

        String nummer = ((MainActivity) getActivity()).getPrefs().getString("number", "");

        MenuItem joinaArrItem = menu.findItem(R.id.joina_arr);
        MenuItem bangaArrItem = menu.findItem(R.id.banga_arr);
        MenuItem luraArrItem = menu.findItem(R.id.lurpassa_arr);
        if (arr != null) {
            for( String deltagare : arr.getDeltagare().split(",") ) {
                if( nummer.equals(deltagare) ) {
                    joinaArrItem.setVisible(false);
                    bangaArrItem.setVisible(true);
                    luraArrItem.setEnabled(false);
                    break;
                }
            }

            for( String lurpassare : arr.getKanske().split(",") ) {
                if( !luraArrItem.isEnabled() || nummer.equals(lurpassare) ) {
                    luraArrItem.setEnabled(false);
                    break;
                }
            }
        }
    }

    @Override
    public final boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Arr arr = arrAdapter.getItem(info.position);

        switch( item.getItemId() ) {
            case R.id.joina_arr:
                if (arr != null) {
                    new JoinArrAsync().execute(arr.getId());
                }
                return true;

            case R.id.banga_arr:
                if (arr != null) {
                    new ExitArrAsync().execute(arr.getId());
                }
                return true;

            case R.id.lurpassa_arr:
                if (arr != null) {
                    new LurpassaArrAsync().execute(arr.getId());
                }
                return true;

             case R.id.hetsa_arr:
                return false;

            case R.id.edit_arr:
                showSkapaEllerUppdateraArr(arr);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showSkapaEllerUppdateraArr(Arr arr) {
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(rootView.getContext());
        helpBuilder.setTitle(arr != null ? "Uppdatera Arr" : "Skapa nytt arr");
        // helpBuilder.setIcon(R.drawable.olsug_32);

        LayoutInflater factory = LayoutInflater.from(rootView.getContext());
        final View textEntryView = factory.inflate(R.layout.popup_new_arr, null);
        helpBuilder.setView(textEntryView);
        final TimePicker timePicker = (TimePicker) textEntryView.findViewById(R.id.arr_popup_timepicker);
        final DatePicker datePicker = (DatePicker) textEntryView.findViewById(R.id.arr_popup_datepicker);
        final EditText arrNamnText = (EditText) textEntryView.findViewById(R.id.arr_popup_arrnamn);
        final EditText arrPlatsText = (EditText) textEntryView.findViewById(R.id.arr_popup_arrplats);
        final Integer arrId = arr == null ? -1 : arr.getId();

        timePicker.setIs24HourView(true);
        timePicker.setCurrentMinute(0);

        if( arr != null ) {
            String dateString = arr.getDatum();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            ParsePosition pos = new ParsePosition(0);
            Calendar date = GregorianCalendar.getInstance();
            date.setTime(formatter.parse(dateString, pos));

            timePicker.setCurrentHour(date.get(Calendar.HOUR_OF_DAY));
            timePicker.setCurrentMinute(date.get(Calendar.MINUTE));
            datePicker.updateDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));

            arrNamnText.setText(arr.getNamn());
            arrPlatsText.setText(arr.getPlats());
        }

        helpBuilder.setNegativeButton("Ångra",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                    }
                });
        helpBuilder.setPositiveButton(arr != null ? "Uppdatera" : "Skapa",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String arrNamn = arrNamnText.getText().toString();
                        String arrPlats = arrPlatsText.getText().toString();
                        if( !arrNamn.isEmpty() || !arrPlats.isEmpty() ) {
                            GregorianCalendar cal = new GregorianCalendar();
                            cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                                    timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                            String date = formatter.format(cal.getTime());

                            Log.d("Arr", "Arr skapas: " + arrNamn + " @ " + arrPlats + " [" + date + "]");

                            RequestArr arr = new RequestArr();
                            arr.setId(arrId);
                            arr.setNamn(arrNamn);
                            arr.setPlats(arrPlats);
                            arr.setDatum(date);
                            arr.setDeltagare(((MainActivity) getActivity()).getPrefs().getString("number", null));

                            new CreateOrUpdateArrAsync().execute(arr);
                        }
                    }
                });

        // Remember, create doesn't show the dialog
        AlertDialog helpDialog = helpBuilder.create();
        helpDialog.show();
    }

    public final class CreateOrUpdateArrAsync extends AsyncTask<Arr, Arr, Void> {
        @Override
        protected Void doInBackground(Arr... arrs) {
            for( Arr arr : arrs ) {
                ((MainActivity) getActivity()).sidanAccess().createOrUpdateArr(
                        arr.getId(), arr.getNamn(), arr.getPlats(), arr.getDatum());
            }
            return null;
        }
    }

    public final class ReadArrAsync extends AsyncTask<String, Void, List<Arr>> {
        @Override
        protected List<Arr> doInBackground(String... strings) {
            // Ta inte bort arret innan det har gått några timmar efter starttiden.
            GregorianCalendar cal = new GregorianCalendar();
            cal.add(GregorianCalendar.HOUR_OF_DAY, -12);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String dateNow = formatter.format(cal.getTime());

            return ((MainActivity) getActivity()).sidanAccess().readArr(dateNow);
        }

        @Override
        protected void onPostExecute(List<Arr> response) {
            if( response.isEmpty() ) {
                Log.d(getClass().getCanonicalName(), "Response was empty from server, will not clear arr.");
                Toast.makeText(rootView.getContext(),
                        "Kunde inte hämta arr från servern.",
                        Toast.LENGTH_SHORT).show();
            } else {
                Log.d(getClass().getCanonicalName(), "Arr from server: " + response.size());
                arrlista.clear();
                arrlista.addAll(response);
                arrAdapter.notifyDataSetChanged();
                arrContainer.setRefreshing(false);
            }
        }
    }

    public final class VotePollAsync extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... yayList) {
            if (currentPoll != null) {
                for (Integer votedOnYay : yayList) {
                    final int id = currentPoll.getId();
                    ((MainActivity) getActivity()).getPrefs().edit()
                            .putInt("lastPollIdAnswered", id).apply();
                    Log.d("Poll", "Röstat " + votedOnYay + " på pollen: " + id);
                    ((MainActivity) getActivity()).sidanAccess().votePoll(id, votedOnYay);

                }
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean voted) {
            if (voted) {
                String text =
                        holder.radioAnswer1.isChecked() ? holder.radioAnswer1.getText().toString() :
                                holder.radioAnswer2.isChecked() ? holder.radioAnswer2.getText().toString() :
                                        "inget!";
                Log.d("Vote", "Röstade på " + text);

                if (holder.radioAnswer1.isChecked() || holder.radioAnswer2.isChecked()) {
                    holder.progressHolder1.setVisibility(View.VISIBLE);
                    holder.progressHolder2.setVisibility(View.VISIBLE);
                    holder.radioAnswer1.setVisibility(View.INVISIBLE);
                    holder.radioAnswer2.setVisibility(View.INVISIBLE);
                    holder.buttonVote.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public final class ReadPollAsync extends AsyncTask<String, Void, Poll> {
        @Override
        protected Poll doInBackground(String... strings) {
            return ((MainActivity) getActivity()).sidanAccess().readPoll();
        }

        @Override
        protected void onPostExecute(Poll receivedPoll) {
            currentPoll = receivedPoll;
            if(currentPoll != null) {
                Log.d(getClass().getCanonicalName(), "Current poll: " + currentPoll.getQuestion());
                lastPollId = currentPoll.getId() > lastPollId ? currentPoll.getId() : lastPollId;

                int lastKnownPollId = currentPoll.getId();
                preferences.edit().putInt("lastKnownPollId", lastKnownPollId).apply();
                String pollQuestion = currentPoll.getQuestion();
                String pollYae = currentPoll.getYae();
                String pollNay = currentPoll.getNay();
                int ansNrYae = currentPoll.getNrYay();
                int ansNrNay = currentPoll.getNrNay();
                int ansTotal = ansNrYae + ansNrNay;

                String fullTextYay = pollYae + " (" + ansNrYae + ")";
                String fullTextNay = pollNay + " (" + ansNrNay + ")";

                /* Add padding to have them centered together.  */
                int maxChars = fullTextYay.length() - fullTextNay.length();
                if (maxChars > 0) {
                    String padding = new String(new char[maxChars]).replace("\0", "  ");
                    fullTextNay = pollNay + padding + " (" + ansNrNay + ")";
                } else if (maxChars < 0) {
                    String padding = new String(new char[-maxChars]).replace("\0", "  ");
                    fullTextYay = pollYae + padding + " (" + ansNrYae + ")";
                }

                holder.txtQuestion.setText(pollQuestion);
                holder.radioAnswer1.setText(pollYae);
                holder.radioAnswer2.setText(pollNay);

                holder.txtProgressAnswer1.setText(fullTextYay);
                holder.txtProgressAnswer2.setText(fullTextNay);
                holder.progressAnswer1.setProgress(ansTotal == 0 ? 0 : ansNrYae * 100 / ansTotal);
                holder.progressAnswer2.setProgress(ansTotal == 0 ? 0 : ansNrNay * 100 / ansTotal);

                int lastPollIdAnswered = preferences.getInt("lastPollIdAnswered", -1);
                if (lastKnownPollId == lastPollIdAnswered) {
                    Log.d(getClass().getCanonicalName(), "Already voted on this poll.");
                    holder.progressHolder1.setVisibility(View.VISIBLE);
                    holder.progressHolder2.setVisibility(View.VISIBLE);
                    holder.radioAnswer1.setVisibility(View.INVISIBLE);
                    holder.radioAnswer2.setVisibility(View.INVISIBLE);
                    holder.buttonVote.setVisibility(View.INVISIBLE);
                } else {
                    holder.progressHolder1.setVisibility(View.INVISIBLE);
                    holder.progressHolder2.setVisibility(View.INVISIBLE);
                    holder.radioAnswer1.setVisibility(View.VISIBLE);
                    holder.radioAnswer2.setVisibility(View.VISIBLE);
                    holder.buttonVote.setVisibility(View.VISIBLE);
                }
            } else {
                Log.d(getClass().getCanonicalName(), "No poll active!");
            }
        }
    }

    public final class JoinArrAsync extends AsyncTask<Integer, Arr, Void> {
        @Override
        protected Void doInBackground(Integer... ids) {
            int id = ids[0];

            Log.d("Arr", "Deltar på arr: " + id);
            ((MainActivity) getActivity()).sidanAccess().registerArr(id);

            return null;
        }
    }

    public final class ExitArrAsync extends AsyncTask<Integer, Arr, Void> {
        @Override
        protected Void doInBackground(Integer... ids) {
            int id = ids[0];

            Log.d("Arr", "Bangar arr: " + id);
            ((MainActivity) getActivity()).sidanAccess().unregisterArr(id);

            return null;
        }
    }

    public final class LurpassaArrAsync extends AsyncTask<Integer, Arr, Void> {
        @Override
        protected Void doInBackground(Integer... ids) {
            int id = ids[0];

            ((MainActivity) getActivity()).sidanAccess().lurpassaArr(id);
            Log.d("Arr", "Lurpassar på arr: " + id);

            return null;
        }
    }
}
