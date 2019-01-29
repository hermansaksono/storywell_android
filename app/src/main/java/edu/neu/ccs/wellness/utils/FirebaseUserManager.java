package edu.neu.ccs.wellness.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by hermansaksono on 1/29/19.
 */

public class FirebaseUserManager {

    // private FirebaseAuth auth;
    private static final String EMAIL = "wellnesstechlab@gmail.com";
    private static final String PASSWORD = "Storywell123";

    private FirebaseUserManager() {
        // this.auth = FirebaseAuth.getInstance();
    }

    public static FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static boolean isLoggedIn() {
        return getUser() != null;
    }

    public static void authenticate(
            Activity activity, final OnCompleteListener<AuthResult> listener) {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(EMAIL, PASSWORD)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>(){
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SWELL", "signInWithEmail:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SWELL", "signInWithEmail:failure", task.getException());
                        }
                        listener.onComplete(task);
                    }
                });
    }
}
