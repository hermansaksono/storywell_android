package edu.neu.ccs.wellness.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.neu.ccs.wellness.server.FirebaseToken;

/**
 * Created by hermansaksono on 1/29/19.
 */

public class FirebaseUserManager {

    private FirebaseUserManager() {
    }

    public static FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static boolean isLoggedIn() {
        return getUser() != null;
    }

    public static void authenticateWithCustomToken(
            Activity activity, FirebaseToken firebaseToken,
            final OnCompleteListener<AuthResult> listener) {
        FirebaseAuth.getInstance()
                .signInWithCustomToken(firebaseToken.getToken())
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>(){
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("SWELL", "signInWithCustomToken:success");
                        } else {
                            Log.w("SWELL", "signInWithCustomToken:failure", task.getException());
                        }
                        listener.onComplete(task);
                    }
                });
    }



}
