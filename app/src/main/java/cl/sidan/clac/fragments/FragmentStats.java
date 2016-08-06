package cl.sidan.clac.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.Stats;

public class FragmentStats extends Fragment {

    private View rootView;
    private List<Stats> statsEnheter = new ArrayList<>();
    private List<Stats> statsGilla = new ArrayList<>();

    private TextView textViewStats;
    private SwipeRefreshLayout statsContainer;


    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_stats, container, false);
        textViewStats = (TextView) rootView.findViewById(R.id.stats_list);
        statsContainer = (SwipeRefreshLayout)  rootView.findViewById(R.id.statsContainer);

        statsContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        statsContainer.setRefreshing(true);
                        new ReadStatsAsync().execute();
                    }
                }
        );
        statsContainer.setRefreshing(true);
        new ReadStatsAsync().execute();

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    public final class ReadStatsAsync extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            List<Stats> tempEnhetsStats = ((MainActivity) getActivity()).sidanAccess().readStats("Enheter");
            List<Stats> tempGillaStats = ((MainActivity) getActivity()).sidanAccess().readStats("Like");

            if (!tempEnhetsStats.isEmpty()) {
                statsEnheter.clear();
                statsEnheter.addAll(tempEnhetsStats);
            }

            if (!tempEnhetsStats.isEmpty()) {
                statsGilla.clear();
                statsGilla.addAll(tempGillaStats);
            }
            return !tempEnhetsStats.isEmpty() && !tempGillaStats.isEmpty();
        }
        @Override
        protected void onPostExecute(Boolean successful) {
            if (successful) {
                /**
                 * SwipeRefreshContainer can only have one child. We can add more if we
                 * make a new class, but I'm simply to lazy to do that.
                 */

                String text = "Enheter i veckan:\n";
                int i = 1;
                for (Stats stat : statsEnheter) {
                    text += i + ". " + stat.getSignature() + " (" + stat.getTotal() + ")\n";
                    i++;
                }
                text += "\nGillade i veckan:\n";
                i = 1;
                for (Stats stat : statsGilla) {
                    text += i + ". " + stat.getSignature() + " (" + stat.getTotal() + ")\n";
                    i++;
                }

                textViewStats.setText(text);
                statsContainer.setRefreshing(false);
            } else {
                Toast.makeText(rootView.getContext(), "Kunde inte hämta statsen. Försök senare.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
