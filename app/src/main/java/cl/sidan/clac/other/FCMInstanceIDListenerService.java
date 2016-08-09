package cl.sidan.clac.other;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import cl.sidan.clac.access.impl.JSONParserSidanAccess;
import cl.sidan.clac.access.interfaces.SidanAccess;

public class FCMInstanceIDListenerService extends FirebaseInstanceIdService {
    private String TAG = this.getClass().getCanonicalName();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken, this);
    }

    public static void sendRegistrationToServer(String token, Context mContext) {
        new RegisterAsyncTask(mContext).execute(token);
    }

    private static class RegisterAsyncTask extends AsyncTask<String, Void, Context> {
        String msg = "";
        boolean success = false;

        Context mContext;

        public RegisterAsyncTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected Context doInBackground(String[] tokens) {
            SharedPreferences prefs = mContext.getSharedPreferences("cl.sidan", 0);

            try {
                String nummer = prefs.getString("username", null);
                String password = prefs.getString("password", null);
                if( nummer == null || password == null ){
                    throw new Exception("Number or password missing.");
                }
                // Send token to the server
                SidanAccess sidanAccess = new JSONParserSidanAccess(nummer, password);
                String androidId = Settings.Secure.getString(
                        mContext.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                sidanAccess.registerGCM(tokens[0], androidId);

                prefs.edit().putBoolean("SENT_TOKEN_TO_SERVER", true).apply();
            } catch (Exception e) {
                Log.d(getClass().getCanonicalName(), "Failed to complete token refresh", e);
                prefs.edit().putBoolean("SENT_TOKEN_TO_SERVER", false).apply();
            }

            return mContext;
        }
    }
}
