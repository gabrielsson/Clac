package cl.sidan.clac.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class FragmentVersionUpdate extends Fragment implements VersionUpdateListener {
    // Versionsnumret sätts från build.gradle.
    private static Long currentMajorVersion = 1L;
    private static Long currentMinorVersion = 0L;

    private static final String externalUrl = "http://sidan.cl/appen/";
    private static final String versionFileName = "latest_version_2.txt";
    private static final String apkFileName = "app-release.apk";

    private Context context;
    private View rootView;

    private static FragmentVersionUpdate versionUpdateFragment;

    public static FragmentVersionUpdate newInstance() {
        if( null == versionUpdateFragment ) {
            versionUpdateFragment = new FragmentVersionUpdate();
        }
        return versionUpdateFragment;
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_version_update, container, false);
        TextView versionInfoText = (TextView) rootView.findViewById(R.id.version_info);
        context = rootView.getContext();

        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            Log.d("VersionUpdate", "VersionName: " + pi.versionName);
            ArrayList<Long> versionList = parseVersionString(pi.versionName);
            currentMajorVersion = versionList.get(0);
            if( versionList.size() >= 2) {
                currentMinorVersion = versionList.get(1);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        versionInfoText.setText(getCurrentVersionString());

        if( ((MainActivity) getActivity()).isConnected() ) {
            VersionUpdateTask vc = new VersionUpdateTask(this);
            vc.execute();
        } else {
            TextView versionStatusText = (TextView) rootView.findViewById(R.id.version_update_status);
            versionStatusText.setText("Uppkoppling saknas!");
        }

        return rootView;
    }

    private String getCurrentVersionString() {
        return getCurrentVersionString(null);
    }

    private String getCurrentVersionString(String latestVersion) {
        String versionInfo = getString(R.string.version_current_version);
        String currentVersion = currentMajorVersion + "." + currentMinorVersion;
        versionInfo = versionInfo.replace("{{currentVersion}}", currentVersion);

        if( latestVersion != null ){
            versionInfo = versionInfo+", "+getString(R.string.version_latest_version).replace("{{latestVersion}}", latestVersion);
        }

        return versionInfo;
    }

    @Override
    public final void onVersionUpdateFailed() {
        Log.d(getClass().getCanonicalName(), ".onVersionUpdateFailed()");
        // Somehow this could get called by another thread than the main thread.
        // This results in an CalledFromWrongThreadException. We solve this by specifying that
        // the text should be set in the main thread.
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run()
            {
                TextView versionUpdateStatusText = (TextView) rootView.findViewById(R.id.version_update_status);
                versionUpdateStatusText.setText(R.string.update_failed);
            }
        });
    }

    @Override
    public final void onVersionUpdateSuccess(boolean isVersionLatest, String versionNumber) {
        TextView versionInfoText = (TextView) rootView.findViewById(R.id.version_info);
        versionInfoText.setText(getCurrentVersionString(versionNumber));

        TextView versionUpdateStatusText = (TextView) rootView.findViewById(R.id.version_update_status);
        if( isVersionLatest ) {
            versionUpdateStatusText.setText(R.string.update_already_latest);
            versionUpdateStatusText.setTextColor(Color.GREEN);
        } else {
            versionUpdateStatusText.setText(R.string.update_exist);

            Update checkUpdate = new Update();
            checkUpdate.execute();

            TextView webView = (TextView) rootView.findViewById(R.id.version_link);
            String url = "<a href=http://sidan.cl/appen/app-release.apk>Ladda ned en ny version</a>";
            webView.setText(Html.fromHtml(url));
            webView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public static ArrayList<Long> parseVersionString(String recieved) {
        ArrayList<Long> versionList = new ArrayList<Long>();
        for(String version : recieved.split("\\.")) {
            versionList.add(Long.parseLong(version));
        }
        return versionList;
    }

    public static class VersionUpdateTask extends AsyncTask<Void, Integer, Long> {

            private String received = null;

            private VersionUpdateListener listener;

            public VersionUpdateTask(VersionUpdateListener listener) {
                this.listener = listener;
            }

            protected final Long doInBackground(Void... voids) {
                Log.d(getClass().getCanonicalName(), "starting...");
                DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());

                String url = externalUrl+versionFileName;
                Log.d(getClass().getCanonicalName(), "Url: " + url);
                HttpGet httpGet = new HttpGet(url);

                InputStream in = null;
                try {
                    HttpResponse response = httpclient.execute(httpGet);
                    HttpEntity entity = response.getEntity();

                    in = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"), 8);

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while( (line = reader.readLine()) != null ){
                        sb.append(line).append("\n");
                    }
                    received = sb.toString().trim();
                    Log.d(getClass().getCanonicalName(), "Received: " + received);
                } catch(Exception e) {
                    Log.e(getClass().getCanonicalName(), "FailedVersionUpdate " + e);
                    listener.onVersionUpdateFailed();
                    return 0L;
                } finally {
                    try{
                        if( in != null )
                            in.close();
                    } catch(Exception e) {
                        // Really don´t care here, let darwin GC fix this.
                    }
                }

                return received.getBytes().length+0L;
            }

            protected final void onPostExecute( Long result ) {
                if( received == null || received.trim().isEmpty() ){
                    listener.onVersionUpdateFailed();
                    return;
                }

                Long majorVersion, minorVersion = 0L;
                try {
                    ArrayList<Long> versionList = parseVersionString(received);
                    majorVersion = versionList.get(0);
                    if( versionList.size() >= 2) {
                        minorVersion = versionList.get(1);
                    }
                } catch (NumberFormatException e) {
                    Log.e(getClass().getCanonicalName(), "FailedVersionUpdate" + e);
                    listener.onVersionUpdateFailed();
                    return;
                }

                boolean isSuccessful = majorVersion.equals(currentMajorVersion)
                        && minorVersion.equals(currentMinorVersion);
                listener.onVersionUpdateSuccess(isSuccessful, received);
            }
    }

    private class Update extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... arg0) {
            try {
                URL url = new URL(externalUrl + apkFileName);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();

                File outputFile = new File(context.getFilesDir().getPath() + "/" + apkFileName);

                if(outputFile.exists()){
                    outputFile.delete();
                }
                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = c.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                }
                fos.close();
                is.close();

                outputFile.setReadable(true, false);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(outputFile), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e("UpdateAPP", "Update error! " + e.getMessage());
            }
            return null;
        }
    }
}