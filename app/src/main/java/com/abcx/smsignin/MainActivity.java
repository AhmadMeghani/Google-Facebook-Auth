package com.abcx.smsignin;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    TextView fbname, fbemail;
    ImageView fbdp;
    SignInButton btnSignIn;
    LoginButton loginButton;
    Button btnSignout;
    GoogleSignInClient mGoogleSignInClient;
    int RC_SIGN_IN = 0;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fbname = findViewById(R.id.fbName);
        fbemail = findViewById(R.id.fbEmail);
        fbdp = findViewById(R.id.fbDP);
        btnSignout = findViewById(R.id.btnSignOut);

        btnSignIn = findViewById(R.id.sign_in_button);
        loginButton = findViewById(R.id.login_button);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if(isLoggedIn){
            loadUserProfile(accessToken);
        }

        loginButton.setLoginBehavior(LoginBehavior.WEB_VIEW_ONLY);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("lol", "signOut: 1st mark");
                switch (v.getId()) {
                    case R.id.sign_in_button:
                        signIn();

                        break;

                }
            }
        });

        btnSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnSignOut:
                        signOut();
                        break;
                }
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        logInWithFacebook();

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode,resultCode,data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if (acct != null) {
                String personName = acct.getDisplayName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();

                fbname.setText(personName);
                fbemail.setText(personEmail);

                // Glide.with(this).load(String.valueOf(personPhoto)).into(dp);
                Glide.with(this).load(personPhoto).into(fbdp);

                btnSignIn.setEnabled(false);
                btnSignout.setEnabled(true);
                loginButton.setEnabled(false);
            }



        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Error", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    public void signOut() {
        Log.d("lol", "signOut: 2nd mark");
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        fbname.setText("");
                        fbemail.setText("");
                        fbdp.setImageResource(0);
                        Toast.makeText(MainActivity.this,"Logged Out Successfully",Toast.LENGTH_SHORT).show();
                        loginButton.setEnabled(true);
                        btnSignIn.setEnabled(true);
                        btnSignout.setEnabled(false);
                    }
                });
    }


    //FACEBOOK LOGIN CODE

    AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if(currentAccessToken==null){
                fbname.setText("");
                fbemail.setText("");
                fbdp.setImageResource(0);
                Toast.makeText(MainActivity.this,"Logged Out Successfully",Toast.LENGTH_SHORT).show();
                btnSignIn.setEnabled(true);
                btnSignout.setEnabled(false);
            }

            else
                loadUserProfile(currentAccessToken);

        }
    };



    public void loadUserProfile(AccessToken accessToken){
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {

                try {
                    String first_name = object.getString("first_name");
                    String last_name = object.getString("last_name");
                    String email = object.getString("email");
                    String id = object.getString("id");
                    //String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";
                    String image_url = "https://graph.facebook.com/"+id+"/picture?type=large&redirect=true&width=600&height=600";


                    fbname.setText(first_name + " " + last_name);
                    fbemail.setText(email);
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.dontAnimate();

                    Glide.with(MainActivity.this).load(image_url).into(fbdp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                btnSignIn.setEnabled(false);
                btnSignout.setEnabled(false);
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields","first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void logInWithFacebook(){
        callbackManager = CallbackManager.Factory.create();

        loginButton.setReadPermissions(Arrays.asList("email","public_profile"));

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                AccessToken accessToken = loginResult.getAccessToken();
                loadUserProfile(accessToken);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    @Override
    protected void onStart() {

        super.onStart();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            updateUI(account);
            loginButton.setEnabled(false);
            btnSignout.setEnabled(true);
            btnSignIn.setEnabled(false);
        }
        else {
            btnSignout.setEnabled(false);
        }

    }

    private void updateUI(GoogleSignInAccount account) {

        String personName = account.getDisplayName();
        String personEmail = account.getEmail();
        String personId = account.getId();
        Uri personPhoto = account.getPhotoUrl();

        fbname.setText(personName);
        fbemail.setText(personEmail);

        // Glide.with(this).load(String.valueOf(personPhoto)).into(dp);
        Glide.with(this).load(personPhoto).into(fbdp);
    }


}
