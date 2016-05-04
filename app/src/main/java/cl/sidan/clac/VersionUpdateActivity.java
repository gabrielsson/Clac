package cl.sidan.clac;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionUpdateActivity extends Activity {
    private static final String APP_SHARED_PREFS = "cl.sidan";
    private SharedPreferences preferences = null;

    private static final String externalUrl = "http://sidan.cl/clac/";
    private static final String apkFileName = "app-release.apk";

    private String upgradeUrl;

    @Override
    protected void onStart() {
        super.onStart();

        preferences = getApplicationContext().getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // make a dialog without a titlebar
        setFinishOnTouchOutside(false); // prevent users from dismissing the dialog by tapping outside
        setContentView(R.layout.activity_version_update);

        Bundle extras = getIntent().getExtras();
        upgradeUrl = extras.getString("UpgradeUrl", externalUrl + apkFileName);
        String upgradeNews = extras.getString("UpgradeNews", "").replace("\\n", "\n");

        TextView viewLog = (TextView) findViewById(R.id.update_news);
        viewLog.setText(upgradeNews);

        Button cancelButton = (Button) findViewById(R.id.update_cancel);
        Button updateButton = (Button) findViewById(R.id.update_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Found
                Long unixTime = System.currentTimeMillis() / 1000; // Unix Epoch Seconds
                preferences.edit().putLong("LastUpdateCheck", unixTime).apply();

                finish();
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(VersionUpdateActivity.this, "Tankar hem uppdatering i bakgrunden.", Toast.LENGTH_LONG).show();
                new UpdateApp().execute(upgradeUrl);
                finish();
            }
        });
    }

    public class UpdateApp extends AsyncTask<String,Void,Boolean> {
        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                URL url = new URL(arg0[0]);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();
                Log.d("XXX_SWO", "Connecting to " + arg0[0]);

                // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                // getExternalCacheDir().getAbsolutePath();
                String outputFilename = getFilesDir().getPath() + "/" + apkFileName;
                File outputFile = new File(outputFilename);

                if(outputFile.exists() && !outputFile.delete()){
                    Log.e("UpdateAPP", "outputFile exists but was unable to delete file.");
                }
                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = c.getInputStream();

                byte[] buffer = new byte[1024];
                int len1;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                }
                fos.close();
                is.close();

                /* Need to be readable from outside of application */
                if (outputFile.setReadable(true, false)) {
                    Log.e("UpdateAPP", "Could not make outputFile readable.");
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(outputFile), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                startActivity(intent);
                return true;
            } catch (Exception e) {
                Log.e("UpdateAPP", "Update error! " + e.getMessage());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Log.d(getClass().getCanonicalName(), "Updating LastUpdateCheck");
                Long unixTime = System.currentTimeMillis() / 1000; // Unix Epoch Seconds
                preferences.edit().putLong("LastUpdateCheck", unixTime).apply();
            } else {
                Log.d(getClass().getCanonicalName(), "Failed with update.");
                Toast.makeText(VersionUpdateActivity.this, "Misslyckades att uppdatera, testa igen senare.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
