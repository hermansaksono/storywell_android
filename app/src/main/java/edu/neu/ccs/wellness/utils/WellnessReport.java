package edu.neu.ccs.wellness.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * Created by hermansaksono on 6/15/18.
 */

public class WellnessReport {

    private final static String TYPE_EMAIL = "message/rfc822";
    private final static String SMS_URI_FORMAT = "sms:%s";
    private final static String KEY_SMS_BODY = "sms_body";

    public static void sendEmailReport(Context context, String to, String subject, String body) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType(TYPE_EMAIL);
        i.putExtra(Intent.EXTRA_EMAIL, to);
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, body);
        try {
            context.startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void sendSMS(Context context, String phoneNumber, String body) {
        String uriString = String.format(SMS_URI_FORMAT, phoneNumber);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
        intent.putExtra(KEY_SMS_BODY, body);
        context.startActivity(intent);
    }
}
