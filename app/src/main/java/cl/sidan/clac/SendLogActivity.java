package cl.sidan.clac;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

public class SendLogActivity extends Activity
{
    private static final String[] RECIPIENTS = new String[]{
            "visnae@gmail.com", "max.gabrielsson@gmail.com", "johan.onsjo@gmail.com "};
    private String errorContent;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // make a dialog without a titlebar
        setFinishOnTouchOutside(false); // prevent users from dismissing the dialog by tapping outside
        setContentView(R.layout.activity_send_log);

        Bundle extras = getIntent().getExtras();
        errorContent = extras.getString("ErrorContent");
        String report = "Grabbarna har kodat fel och du har upptäckt en bugg...\n\n" + errorContent;

        TextView viewLog = (TextView) findViewById(R.id.error_view_log);
        viewLog.setText(report);

        Button sendLogButton = (Button) findViewById(R.id.error_mail_button);
        Button cancelButton = (Button) findViewById(R.id.error_mail_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(1);
            }
        });

        sendLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLogMail();
            }
        });
    }

    private void sendLogMail ()
    {
        Intent sendIntent = new Intent(Intent.ACTION_SEND); // Mail

        sendIntent.setType("plain/text");
        sendIntent.putExtra(Intent.EXTRA_EMAIL, RECIPIENTS);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getSubject());
        sendIntent.putExtra(Intent.EXTRA_TEXT, getLogText());

        startActivity(Intent.createChooser(sendIntent, "Error Report"));
    }

    private String getSubject() {
        return "Clac krash " + (new Date()).toString();
    }

    private String getLogText() {
        String NL = "\n";

        return NL + NL + errorContent + NL + NL;
    }
}
