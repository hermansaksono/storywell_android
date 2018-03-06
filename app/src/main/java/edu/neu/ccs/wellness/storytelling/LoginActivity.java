package edu.neu.ccs.wellness.storytelling;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import edu.neu.ccs.wellness.server.OAuth2Exception;

import static edu.neu.ccs.wellness.storytelling.storyview.ReflectionFragment.isPermissionGranted;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {

    // Error codes
    private enum LoginResponse {
        SUCCESS, WRONG_CREDENTIALS, NO_INTERNET, IO_ERROR
    }

    ;
    //Request Audio Permissions as AUDIO RECORDING falls under DANGEROUS PERMISSIONS
    public final int REQUEST_AUDIO_PERMISSIONS = 100;
    private String[] permission = {android.Manifest.permission.RECORD_AUDIO};


    // Private variables
    private UserLoginAsync mAuthTask = null;
    private Storywell storywell;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        storywell = new Storywell(getApplicationContext());

        setContentText();

        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_SEND) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        //mUsernameView.setText("family01");
        //mPasswordView.setText("tacos000");

        Button mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        ActivityCompat.requestPermissions(LoginActivity.this, permission, REQUEST_AUDIO_PERMISSIONS);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginAsync(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isUsernameValid(String email) {
        return email.length() > 4;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private void startSplashScreenActivity() {
        Intent intent = new Intent(this, SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /***
     * Set the text for the Login screen
     */
    private void setContentText() {
        Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(),
                StoryViewActivity.STORY_TEXT_FACE);
        TextView tv = (TextView) findViewById(R.id.text);
        tv.setTypeface(tf);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginAsync extends AsyncTask<Void, Void, LoginResponse> {

        private final String username;
        private final String password;

        UserLoginAsync(String email, String password) {
            this.username = email;
            this.password = password;
        }

        @Override
        protected LoginResponse doInBackground(Void... params) {
           // Log.i("WELL Logging in user", this.username);
            try {
                //TODO what if no internet??
                storywell.loginUser(this.username, this.password);
                return LoginResponse.SUCCESS;
            } catch (OAuth2Exception e) {
             //   Log.e("WELL OAuth2", e.toString());
                return LoginResponse.WRONG_CREDENTIALS;
            } catch (IOException e) {
              //  Log.e("WELL OAuth2", e.toString());
                return LoginResponse.IO_ERROR;
            }
        }

        @Override
        protected void onPostExecute(final LoginResponse response) {
            mAuthTask = null;
            showProgress(false);
            Log.i("WELL Login", response.toString());
            if (response.equals(LoginResponse.SUCCESS)) {
                startSplashScreenActivity();
            } else if (response.equals(LoginResponse.WRONG_CREDENTIALS)) {
                mPasswordView.setError(getString(R.string.error_incorrect_cred));
                mPasswordView.requestFocus();
            } else if (response.equals(LoginResponse.IO_ERROR)) {
            }
        }


        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }//End of Async Task


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Get the requestCode and check our case
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSIONS:
                //If Permission is Granted, change the boolean value
                if (grantResults.length > 0) {
                    isPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                } else {
                    Snackbar permissionsSnackBar =
                            Snackbar.make(findViewById(android.R.id.content), "Audio Permission needed",
                                    Snackbar.LENGTH_LONG);
                    permissionsSnackBar.setAction("Try Again", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(LoginActivity.this,
                                    permission, REQUEST_AUDIO_PERMISSIONS);
                        }
                    });
                }
                break;
        }
    }
}