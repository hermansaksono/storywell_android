package edu.neu.ccs.wellness.server;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by hermansaksono on 3/6/19.
 */

public class FirebaseToken {

    private static final long ONE_SECOND_IN_MILLISECONDS = 1000;

    private String token = "";

    @SerializedName("expires_at_timestamp")
    private long expiresAtTimestamp = 0;

    public FirebaseToken(String token, long expiresAtTimestamp) {
        this.token = token;
        this.expiresAtTimestamp = expiresAtTimestamp;
    }

    public FirebaseToken() {
    }

    public String getToken() {
        return token;
    }

    public long getExpiresAtTimestamp() {
        return expiresAtTimestamp;
    }

    public boolean isSet() {
        return !this.token.isEmpty();
    }

    public Calendar getExpiresAt() {
        Calendar cal = Calendar. getInstance(Locale.US);
        cal.setTimeInMillis(this.expiresAtTimestamp * ONE_SECOND_IN_MILLISECONDS);
        return cal;
    }
}
