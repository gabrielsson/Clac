package cl.sidan.clac.other;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.firebase.auth.api.model.StringList;
import com.google.firebase.crash.FirebaseCrash;

import java.lang.Thread.UncaughtExceptionHandler;

import cl.sidan.clac.R;

public class FirebaseExceptionHandler implements UncaughtExceptionHandler {
    final static String errorMsg = "Grabbarna har kodat fel. Rapporterar!";
    final static String errorTitle = "Clac";

    private Context context;

    public FirebaseExceptionHandler(Context ctx) {
        context = ctx;
    }

    @Override
    public final void uncaughtException(Thread t, Throwable e) {
        FirebaseCrash.report(e);

        if (isUIThread()) {
            sendNotification(errorTitle, errorMsg);
        } else {  // Handle non UI thread throw uncaught exception
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    sendNotification(errorTitle, errorMsg);
                }
            });
        }

        System.exit(1);
    }

    public boolean isUIThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    private void sendNotification(String user, String message) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                // .setColor(R.color.colorPrimaryPink)
                .setSmallIcon(R.drawable.nopo)
                .setContentTitle("Message from " + user)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0));

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
