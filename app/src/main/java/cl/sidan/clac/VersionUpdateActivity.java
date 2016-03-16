package cl.sidan.clac;

import android.app.Activity;
import android.content.Intent;
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

    private static final String externalUrl = "http://sidan.cl/appen/";
    private static final String apkFileName = "app-release.apk";

    private String upgradeUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // make a dialog without a titlebar
        setFinishOnTouchOutside(false); // prevent users from dismissing the dialog by tapping outside
        setContentView(R.layout.activity_version_update);

        Bundle extras = getIntent().getExtras();
        upgradeUrl = extras.getString("UpgradeUrl", externalUrl + apkFileName);
        String upgradeNews = extras.getString("UpgradeNews", "");
        String report = "News:" + upgradeNews;

        TextView viewLog = (TextView) findViewById(R.id.update_news);
        viewLog.setText(report);

        Button cancelButton = (Button) findViewById(R.id.update_cancel);
        Button updateButton = (Button) findViewById(R.id.update_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadVersionUpdate(upgradeUrl);
            }
        });
    }

    private void downloadVersionUpdate(String url) {
        new UpdateApp().execute(url);
    }

    public class UpdateApp extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... arg0) {
            try {
                URL url = new URL(arg0[0]);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();

                // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                // getExternalCacheDir().getAbsolutePath();
                File outputFile = new File(getFilesDir().getPath() + "/" + apkFileName);

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

                /*
                if (outputFile.setReadable(true, false)) {
                    Log.e("UpdateAPP", "Could not make outputFile readable.");
                }
                */

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(outputFile), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                startActivity(intent);
            } catch (Exception e) {
                Log.e("UpdateAPP", "Update error! " + e.getMessage());
                // Toasts must be run on UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(VersionUpdateActivity.this, "Kunde inte hitta uppdateringsfilen, kontakta någon!", Toast.LENGTH_LONG).show();
                    }
                });
                finish();
            }
            return null;
        }
    }
}
