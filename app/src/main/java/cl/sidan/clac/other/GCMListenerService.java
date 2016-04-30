package cl.sidan.clac.other;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.io.UnsupportedEncodingException;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.R;

public class GCMListenerService extends GcmListenerService {

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String base64message = data.getString("message");
        String title = data.getString("title");

        Log.d(getClass().getCanonicalName(), "Notification with title \"" + title + "\" received from " + from + ": " + base64message);

        String user = "Clac",
                message = "";
        try {
            byte[] decodedMessage = Base64.decode(base64message, Base64.DEFAULT);
            message = new String(decodedMessage, "UTF-8");

            /* JSB encodes, and google encodes.... */
            decodedMessage = Base64.decode(message, Base64.DEFAULT);
            message = new String(decodedMessage, "UTF-8");

            Log.d(getClass().getCanonicalName(), "Received message from GCM: " + message);
        } catch (UnsupportedEncodingException e) {
            Log.d(getClass().getCanonicalName(), "UnsupportedEncoding");
            e.printStackTrace();
        }

        if (null == title || title.equals("")) {
            title = user;
        }

        sendNotification(title, message);
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String user, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                // .setColor(R.color.colorPrimaryPink)
                .setSmallIcon(R.drawable.nopo)
                .setContentTitle("Message from " + user)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}