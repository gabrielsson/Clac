package cl.sidan.clac.other;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import cl.sidan.clac.MainActivity;
import cl.sidan.clac.R;

public class FCMListenerService extends FirebaseMessagingService {
    private String TAG = getClass().getCanonicalName();


    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String fromID = remoteMessage.getFrom(),
                body;

        Map data = remoteMessage.getData();

        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "FromID: " + fromID);

        // Check if message contains a data payload.
        String from = "Clac", message = "";
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + data);
            Object title = data.get("title");
            if (null != title) {
                from = title.toString();
            }
            Object messageBody = data.get("message");
            if (null != messageBody) {
                message = messageBody.toString();
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Message Notification Body: " + body);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        sendNotification(from, message);
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

        /*
         * Something like this would add a reply button below the notification.
         *

        // Key for the string that's delivered in the action's intent.
        String KEY_TEXT_REPLY = "key_text_reply";
        String replyLabel = "Reply";
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel(replyLabel)
                .build();

        // Create the reply action and add the remote input.
        Notification.Action replyAction = new Notification.Action.Builder(
                R.drawable.smiley6, "Gilla", pendingIntent)
                .addRemoteInput(remoteInput)
                .build();
         */

        Notification.Builder notificationBuilder = new Notification.Builder(this)
                // .setColor(R.color.colorPrimaryPink)
                .setSmallIcon(R.drawable.nopo)
                .setContentTitle("Message from " + user)
                .setContentText(message)
                .setStyle(new Notification.BigTextStyle().bigText(message)) // Multiline
                // .addAction(replyAction)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setLights(0xff00ff00, 300, 500)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(
                (int) System.currentTimeMillis() /* (unique) ID of notification */,
                notificationBuilder.build());
    }
}