package cl.sidan.clac.fragments;


import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.Locale;

public class MyExceptionHandler implements UncaughtExceptionHandler {
    private final String NL = "\n";

    private static final String[] RECIPIENTS = new String[]{
            "sebastiw@student.chalmers.se", "max.gabrielsson@gmail.com", "johan.onsjo@gmail.com "};
    private Context context;
    private static Context context1;

    public MyExceptionHandler(Context ctx) {
        context = ctx;
        context1 = ctx;
    }

    private StatFs getStatFs() {
        File path = Environment.getDataDirectory();
        return new StatFs(path.getPath());
    }

    private long getAvailableInternalMemorySize(StatFs stat) {
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    private long getTotalInternalMemorySize(StatFs stat) {
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return totalBlocks * blockSize;
    }

    private void addHWInformation(StringBuilder message) {
        message.append("Phone Model: ").append(Build.MODEL).append(NL);
        message.append("Android Version: ").append(Build.VERSION.RELEASE).append(NL);
        message.append("Brand: ").append(Build.BRAND).append(NL);
        message.append("Device: ").append(Build.DEVICE).append(NL);
        message.append("ID: ").append(Build.ID).append(NL);
        message.append("Product: ").append(Build.PRODUCT).append(NL);
        message.append("Model: ").append(Build.MODEL).append(NL);
        message.append("Board: ").append(Build.BOARD).append(NL);
        message.append("Host: ").append(Build.HOST).append(NL);
        message.append("Type: ").append(Build.TYPE).append(NL);
        StatFs stat = getStatFs();
        message.append("Total Internal memory: ").append(
                getTotalInternalMemorySize(stat)).append(NL);
        message.append("Available Internal memory: ").append(
                getAvailableInternalMemorySize(stat)).append(NL);
    }

    private void addSWInformation(StringBuilder message) {
        message.append("Locale: ").append(Locale.getDefault()).append(NL);

        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            message.append("Package: ").append(pi.packageName).append(NL);
            message.append("Version name: ").append(pi.versionName).append(NL);
            message.append("Version code: ").append(pi.versionCode).append(NL);
            message.append("Native Heap Size: ").append(Debug.getNativeHeapSize()).append(NL);
            message.append("Native Heap Free Size: ").append(
                    Debug.getNativeHeapFreeSize()).append(NL);
            message.append("Native Heap Allocated Size: ").append(
                    Debug.getNativeHeapAllocatedSize()).append(NL);
        } catch (Exception e) {
            Log.e("CustomExceptionHandler", "Error", e);
            message.append("Could not get Version information for ").append(
                    context.getPackageName());
        }

        message.append("SDK: ").append(Build.VERSION.SDK_INT).append(NL);
        message.append("Incremental: ").append(Build.VERSION.INCREMENTAL).append(NL);
    }

    @Override
    public final void uncaughtException(Thread t, Throwable e) {
        try {
            StringBuilder report = new StringBuilder();
            Date curDate = new Date();
            report.append("Error Report collected on : ").append(curDate.toString()).append(NL).append(NL);
            report.append("****** ADDITIONAL INFORMATION ******").append(NL);
            report.append("To be filled in by user....").append(NL).append(NL).append(NL);

            report.append("****** DEVICE INFORMATION ******").append(NL);
            addHWInformation(report);
            report.append(NL).append(NL);

            report.append("****** FIRMWARE ******").append(NL);
            addSWInformation(report);
            report.append(NL).append(NL);

            report.append("****** CAUSE OF ERROR ******").append(NL);
            report.append("Is user a monkey? ").append(ActivityManager.isUserAMonkey() ? "Yes" : "No")
                    .append(NL).append(NL);

            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            report.append(result.toString());
            printWriter.close();
            report.append(NL).append(NL);

            // report.append("****** EXTENDED STACKTRACE ******").append(NL).append(NL);
            // TO BE ADDED
            //report.append(NL);

            report.append("****** END OF REPORT ******");

            Log.e(MyExceptionHandler.class.getName(), "Error while sendErrorMail. " + report);
            sendErrorMail(report);
        } catch (Throwable ignore) {
            Log.e(MyExceptionHandler.class.getName(), "Error while sending error e-mail", ignore);
        }
    }

    public void sendErrorMail(final StringBuilder errorContent) {
        final AlertDialog.Builder builder= new AlertDialog.Builder(context);
        new Thread(){
            @Override
            public void run() {
                Looper.prepare();
                builder.setTitle("Taskigt läge!");
                builder.create();
                builder.setMessage("Grabbarna har kodat fel och du har upptäckt en bugg...");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        System.exit(1);
                    }
                });
                builder.setPositiveButton("Rapportera", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);

                        Date curDate = new Date();
                        String subject = "CLappen krashade " + curDate.toString();

                        sendIntent.setType("plain/text");
                        sendIntent.putExtra(Intent.EXTRA_EMAIL, RECIPIENTS);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, NL + NL + errorContent + NL + NL);
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

                        context1.startActivity(Intent.createChooser(sendIntent, "Error Report"));
                        System.exit(0);
                    }
                });
                // XXX_TODO:
                // Vi har fått en felrapport på builder.show():
                // android.view.WindowManager$BadTokenException:
                // Unable to add window -- token android.os.BinderProxy@3dd7bd7d is not valid;
                // is your activity running?
                // Detta beror på att vi försöker visa ett dialog meddelande i en asynkron tråd.
                // Flödet är följande:
                // 1. Tråd två kör igång och försöker göra arbete
                // 2. Tråd ett med aktiviteten dör
                // 3. Tråd två vill visa en dialog i aktiviteten.
                // Poff!
                builder.show();
                Looper.loop();
            }
        }.start();
    }
}
