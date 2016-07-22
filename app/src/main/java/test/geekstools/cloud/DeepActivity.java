package test.geekstools.cloud;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;


public class DeepActivity extends Activity {

    private GoogleApiClient mClient;
    private Uri mUrl;
    private String mTitle;
    private String mDescription;

    private static final Uri BASE_URL = Uri.parse("android-app://test.geekstools.cloud/http/geeksempire.net/tools/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep);

        mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        mUrl = BASE_URL;
        mTitle = "Fuck";
        mDescription = "GeeksEmpire Tools | Cloud Description";

    }

    @Override
    public void onStart() {
        super.onStart();

        mClient.connect();
        AppIndex.AppIndexApi.start(mClient, getAction());

        Uri APP_URI = BASE_URL.buildUpon().appendPath(mTitle).build(); //URI for Activity
        final Action viewAction = Action.newAction(Action.TYPE_VIEW, mTitle, APP_URI);
        System.out.println("URI >> " + APP_URI + " ||  ID >> " + "" + " || Title >> " +  mTitle + " INDEXED");

        // Call the App Indexing API view method
        final PendingResult<Status> result = AppIndex.AppIndexApi.start(mClient, viewAction);

        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d(getPackageName(), "App Indexing API: Indexed " + "" + " view successfully.");

                    String data = getIntent().getDataString();
                    System.out.println("DeepResult >>> ");
                    System.out.println("DeepResult >>> After Slash >> " + data.substring(data.lastIndexOf("/") + 1));
                }
                else {
                    Log.e(getPackageName(), "App Indexing API: There was an error indexing the recipe view."
                            + status.toString());
                }
            }
        });
    }

    @Override
    public void onStop() {
        AppIndex.AppIndexApi.end(mClient, getAction());
        mClient.disconnect();
        super.onStop();
    }

    public Action getAction() {
        Thing thing = new Thing.Builder()
                .setName(mTitle)
                .setDescription(mDescription)
                .setUrl(mUrl)
                .build();

        Action action = new Action.Builder(Action.TYPE_VIEW)
                .setObject(thing)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();

        return action;

    }
}
