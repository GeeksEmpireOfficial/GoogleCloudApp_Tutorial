package test.geekstools.cloud;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "rNN8ULGDzXpxGBKjs2FjemRce";
    private static final String TWITTER_SECRET = "NW9wiWbhDb04NrUOlZ2Dvb1IHBPqt7u5k6hFQlO9uUGlyQCwIV";


    Button login, upload;
    TwitterLoginButton twitter;
    SignInButton google;
    EditText data, user, password;
    ListView listView;
    ArrayAdapter<String> adapter;

    GoogleApiClient mGoogleApiClient;

    String FirebaseLink = "https://boiling-inferno-1433.firebaseio.com/";
    String FirebaseTitle = "GeeksEmpire";
    String FirebaseSub = "gX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(getApplicationContext());

        upload = (Button)findViewById(R.id.upload);
        login = (Button)findViewById(R.id.login);
        twitter = (TwitterLoginButton)findViewById(R.id.twitter);
        google = (SignInButton)findViewById(R.id.gplus);

        data = (EditText)findViewById(R.id.clientData);
        user = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);

        listView = (ListView)findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        listView.setAdapter(adapter);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Firebase(FirebaseLink + FirebaseTitle)
                        .push()
                        .child(FirebaseSub)
                        .setValue(data.getText().toString());

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("message");
                myRef.setValue("Hello, World!");

                DatabaseReference mDatabase =  FirebaseDatabase.getInstance().getReference();
                mDatabase.child("child1").child("child2").setValue("value");


                Toast.makeText(getApplicationContext(), "DATA >> " + data.getText().toString(), Toast.LENGTH_LONG).show();
                System.out.println("DATA >> " + data.getText().toString());
            }
        });

        //Get Info
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ParentNote = new Firebase(FirebaseLink + FirebaseTitle).getParent().toString();
                String KeyNote = new Firebase(FirebaseLink + FirebaseTitle).getKey().toString();
                String AppNote = new Firebase(FirebaseLink + FirebaseTitle).getApp().toString();

                Toast.makeText(getApplicationContext(), "ParentNote >> " + ParentNote +
                                "\n" +
                                "KeyNote >> " + KeyNote +
                                "\n" +
                                "AppNote >> " + AppNote
                        , Toast.LENGTH_LONG).show();
            }
        });

        // Delete items when clicked
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                new Firebase(FirebaseLink + FirebaseTitle)
                        .orderByChild("notes")
                        .equalTo((String) listView.getItemAtPosition(position))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChildren()) {
                                    DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                    firstChild.getRef().removeValue();
                                }
                            }
                            public void onCancelled(FirebaseError firebaseError) { }
                        });

                return false;
            }
        });

        /*TWITTER Sign-In*/
        twitter.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Toast.makeText(getApplicationContext(), result.data.getUserName(), Toast.LENGTH_LONG).show();
                SetUpContent();
            }

            @Override
            public void failure(TwitterException exception) {

            }
        });

        /*GOOGLE Sign-In*/
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        //E-MAIL Login
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password.getText().toString().length() > 5){
                    String email = user.getText().toString();
                    String pass = password.getText().toString();

                    Firebase ref = new Firebase(FirebaseLink);
                    ref.authWithPassword(email, pass, new Firebase.AuthResultHandler() {
                        @Override
                        public void onAuthenticated(AuthData authData) {
                            System.out.println("User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
                            Toast.makeText(getApplicationContext(), "User Authenticated", Toast.LENGTH_LONG).show();

                            SetUpContent();
                        }
                        @Override
                        public void onAuthenticationError(FirebaseError firebaseError) {
                            Toast.makeText(getApplicationContext(), "User Created", Toast.LENGTH_LONG).show();
                            String email = user.getText().toString();
                            String pass = password.getText().toString();

                            Firebase ref = new Firebase(FirebaseLink);
                            ref.createUser(email, pass, new Firebase.ValueResultHandler<Map<String, Object>>() {
                                @Override
                                public void onSuccess(Map<String, Object> result) {
                                    System.out.println("Successfully created user account with uid: " + result.get("uid"));
                                }
                                @Override
                                public void onError(FirebaseError firebaseError) {
                                    // there was an error
                                }
                            });
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(), "Insert 5 Characters Password", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();

        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_unit_id));

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("TEST_XXX")
                .build();
        adRequest.isTestDevice(getApplicationContext());
        mAdView.loadAd(adRequest);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("" + requestCode + " > " + resultCode + " > " + data + "");

        if(requestCode == 7){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if(result.isSuccess()){
                //Handle Security Inside App
                GoogleSignInAccount acct = result.getSignInAccount();

                Toast.makeText(getApplicationContext(), "getEmail >> " + acct.getEmail(), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "getDisplayName >> " + acct.getDisplayName(), Toast.LENGTH_LONG).show();
                SetUpContent();
            }
        }
        else if(requestCode == 777){
            // Get the invitation IDs of all sent messages
            String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
            for (String id : ids) {
                Log.d("", "onActivityResult: sent invitation " + id);
            }

        }
        else{
            twitter.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, 7);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "onConnectionFailed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;

        switch (item.getItemId()) {
            case R.id.storage: {
                startActivity(new Intent(getApplicationContext(), StorageFirebase.class));

                break;
            }
            case R.id.notification:{
                startActivity(new Intent(getApplicationContext(), NotificationFirebase.class));

                break;
            }
            case R.id.remoteconfig:{
                startActivity(new Intent(getApplicationContext(), RemoteConfigFirebase.class));

                break;
            }
            case R.id.backupapi:{
                startActivity(new Intent(getApplicationContext(), BackupAPI.class));

                break;
            }
            case R.id.invite:{
                onInviteClicked();
            }
            case R.id.dynamic:{
                startActivity(new Intent(getApplicationContext(), DynamicActivity.class));

                break;
            }
            default: {
                result = super.onOptionsItemSelected(item);
                break;
            }
        }
        return result;
    }

    public void SetUpContent(){
        Firebase.setAndroidContext(this);
        new Firebase(FirebaseLink + FirebaseTitle).addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                System.out.println((String)dataSnapshot.child(FirebaseSub).getValue());
                adapter.add((String)dataSnapshot.child(FirebaseSub).getValue());
            }
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                adapter.remove((String)dataSnapshot.child(FirebaseSub).getValue());
            }
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            public void onCancelled(FirebaseError firebaseError) { }
        });
    }

    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder("invitation_title")
                .setMessage("invitation_message")
        //        .setDeepLink(Uri.parse("https://play.google.com/store/apps/details?id=net.geekstools.floatshort.PRO"))
        //        .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
        //        .setCallToActionText("invitation_cta")
                .build();
        startActivityForResult(intent, 777);
    }
}
