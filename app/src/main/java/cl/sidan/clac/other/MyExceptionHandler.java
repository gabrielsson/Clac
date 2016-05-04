package cl.sidan.clac.other;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
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

    private Context context;

    public MyExceptionHandler(Context ctx) {
        context = ctx;
    }

    @Override
    public final void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically

        final StringBuilder report = getErrorContent(e);

        if(isUIThread()) {
            invokeLogActivity(report);
        } else {  // Handle non UI thread throw uncaught exception
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    invokeLogActivity(report);
                }
            });
        }
    }

    public boolean isUIThread(){
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    private void invokeLogActivity(StringBuilder report) {
        Intent errorIntent = new Intent("cl.sidan.clac.SEND_LOG");
        errorIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        errorIntent.putExtra("ErrorContent", report.toString());

        context.startActivity(errorIntent);

        System.exit(1);
    }

    private StringBuilder getErrorContent(Throwable e) {
        StringBuilder report = new StringBuilder();
        Date curDate = new Date();
        report.append("Error Report collected on : ").append(curDate.toString()).append(NL).append(NL);

        report.append("****** DEVICE INFORMATION ******").append(NL);
        addHWInformation(report);
        report.append(NL).append(NL);

        report.append("****** FIRMWARE ******").append(NL);
        addSWInformation(report);
        report.append(NL).append(NL);

        report.append("****** CAUSE OF ERROR ******").append(NL);
        report.append("Is user a monkey? ").append(ActivityManager.isUserAMonkey() ? "Yes" : "No")
                .append(NL);
        report.append("UI thread? ").append(isUIThread() ? "Yes" : "No").append(NL).append(NL);

        // Print Stacktrace
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        report.append(result.toString());
        printWriter.close();
        report.append(NL).append(NL);

        // report.append("****** EXTENDED STACKTRACE ******").append(NL).append(NL);
        // TO BE ADDED
        // report.append(NL);

        report.append("****** ADDITIONAL INFORMATION ******").append(NL);
        report.append("To be filled in by user....").append(NL).append(NL).append(NL);

        report.append("****** END OF REPORT ******");

        return report;
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
}
