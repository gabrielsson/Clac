package cl.sidan.clac.other;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import cl.sidan.clac.access.impl.JSONParserSidanAccess;
import cl.sidan.clac.access.interfaces.SidanAccess;
import cl.sidan.clac.interfaces.GCMChangeListener;

public class GCMUtil {
    public static final String PREFS_REG_ID_KEY = "gcm_reg_id";

    @SuppressWarnings("unchecked")
    public static void register( final Context context ) {
        new RegisterAsyncTask(context).execute();
    }

    private static class RegisterAsyncTask extends AsyncTask<Context, Void, Context> {
        String msg = "";
        boolean success = false;

        String GOOGLE_PROJECT_ID = "clappen-clac";
        String TAG = "GCMService";
        private Context mContext;

        public RegisterAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected Context doInBackground(Context[] objects) {
            SharedPreferences prefs = mContext.getSharedPreferences("cl.sidan", 0);

            try {
                String nummer = prefs.getString("username", null);
                String password = prefs.getString("password", null);
                if( nummer == null || password == null ){
                    throw new Exception("Number or password missing.");
                }

                // [START register_for_gcm]
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                // [START get_token]
                InstanceID instanceID = InstanceID.getInstance(mContext);

//                String iid = instanceID.getId();
                String token = instanceID.getToken(GOOGLE_PROJECT_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
//                String regId = gcm.register(GOOGLE_PROJECT_NUMBER);
                // [END get_token]
                Log.i(TAG, "GCM Registration Token: " + token);

                // Send token to the app server
                SidanAccess sidanAccess = new JSONParserSidanAccess(nummer, password);
                String androidId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                sidanAccess.registerGCM(token, androidId);

                // Persist the registration - no need to register again.
                prefs.edit().putBoolean("SENT_TOKEN_TO_SERVER", true).apply();
                prefs.edit().putString(PREFS_REG_ID_KEY, token);

                // [END register_for_gcm]
            } catch (Exception e) {
                Log.d(TAG, "Failed to complete token refresh", e);
                // If an exception happens while fetching the new token or updating our registration data
                // on a third-party server, this ensures that we'll attempt the update at a later time.
                prefs.edit().putBoolean("SENT_TOKEN_TO_SERVER", false).apply();
            }
            // Notify UI that registration has completed, so the progress indicator can be hidden.

            return mContext;
        }

        @Override
        protected void onPostExecute(Context context) {
            if( context instanceof GCMChangeListener){
                ((GCMChangeListener) context).onGCMChange(success, msg);
            }
            super.onPostExecute(context);
        }
    }
}
