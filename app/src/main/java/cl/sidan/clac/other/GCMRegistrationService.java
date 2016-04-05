package cl.sidan.clac.other;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

import cl.sidan.clac.R;
import cl.sidan.clac.access.impl.JSONParserSidanAccess;
import cl.sidan.clac.access.interfaces.SidanAccess;
import cl.sidan.clac.interfaces.GCMChangeListener;

public class GCMRegistrationService extends InstanceIDListenerService {
    public static final String PREFS_REG_ID_KEY = "gcm_reg_id";

    @Override
    public void onTokenRefresh() {
        register(this);
    }

    public static void register( final Context context ) {
        new RegisterAsyncTask(context).execute();
    }

    private static class RegisterAsyncTask extends AsyncTask<Context, Void, Context> {
        String msg = "";
        boolean success = false;

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

                InstanceID instanceID = InstanceID.getInstance(mContext);
                String token = instanceID.getToken(
                        mContext.getString(R.string.gcm_defaultSenderId),
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE);
                Log.i(getClass().getCanonicalName(), "GCM Registration Token: " + token);

                // Send token to the server
                SidanAccess sidanAccess = new JSONParserSidanAccess(nummer, password);
                String androidId = Settings.Secure.getString(
                        mContext.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                sidanAccess.registerGCM(token, androidId);

                prefs.edit().putBoolean("SENT_TOKEN_TO_SERVER", true).apply();
            } catch (Exception e) {
                Log.d(getClass().getCanonicalName(), "Failed to complete token refresh", e);
                prefs.edit().putBoolean("SENT_TOKEN_TO_SERVER", false).apply();
            }

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
