package com.kevinersoy.androidoreovibrationbuilder;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.kevinersoy.androidoreovibrationbuilder.db.room.Profile;
import com.kevinersoy.androidoreovibrationbuilder.db.room.ProfileDao;
import com.kevinersoy.androidoreovibrationbuilder.ui.VibrationProfileListActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.kevinersoy.androidoreovibrationbuilder.provider.VibrationBuilderProviderContract.Profiles;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * This class will be used to upload profiles to the server, restore my profiles from the server
 * and (later) fetch other peoples profiles from the server
 */
public class ProfileSyncService extends IntentService {

    public static final String TAG = ProfileSyncService.class.getSimpleName();

    // Upload Profile/Profiles
    private static final String ACTION_UPLOAD = "com.kevinersoy.androidoreovibrationbuilder.action.UPLOAD";
    // Fetch other profiles from server, or restore own if EXTRA_RESTORE
    private static final String ACTION_FETCH = "com.kevinersoy.androidoreovibrationbuilder.action.FETCH";


    private static final String EXTRA_PROFILE_ID = "com.kevinersoy.androidoreovibrationbuilder.extra.PROFILE_ID";
    private static final String EXTRA_RESTORE = "com.kevinersoy.androidoreovibrationbuilder.extra.RESTORE";
    public static final String ALL_PROFILES = "ALL_PROFILES";


    public ProfileSyncService() {
        super("ProfileSyncService");
    }

    /**
     * Starts this service to perform action Upload with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    public static void startActionUpload(Context context, String profileId) {
        Intent intent = new Intent(context, ProfileSyncService.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(EXTRA_PROFILE_ID, profileId);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Fetch with the given parameters. If
     * the service is already performing a task this action will be queued.
     * If restoreOnly is true, it will restore only this user's profiles.
     *
     * @see IntentService
     */

    public static void startActionFetch(Context context, Boolean restoreOnly) {
        Intent intent = new Intent(context, ProfileSyncService.class);
        intent.setAction(ACTION_FETCH);
        intent.putExtra(EXTRA_RESTORE, restoreOnly);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD.equals(action)) {
                final String profileId = intent.getStringExtra(EXTRA_PROFILE_ID);
                handleActionUpload(profileId);
            } else if (ACTION_FETCH.equals(action)) {
                final Boolean restoreOnly = intent.getBooleanExtra(EXTRA_RESTORE, true);
                try{
                    handleActionFetch(restoreOnly);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "JSON parse failed");
                }
            }
        }
    }


    /**
     * Handle action Upload in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpload(String profileId) {
        ProfileDao profileDao = DataManager.getInstance().getDB(getApplicationContext()).profileDao();

        if(!profileId.equals(ALL_PROFILES)){
            profileDao.findById(Integer.parseInt(profileId))
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(profile -> upload(profile),
                            Throwable::printStackTrace);
        } else {
            // All Profiles
            profileDao.getProfiles()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(profiles -> upload(profiles));
        }
    }

    private void upload(List<Profile> profiles){
        for(Profile profile : profiles){
            upload(profile);
        }
    }

    private void upload(Profile profile){
        if("example".equals(profile.getGuid())){
            return;  // Don't sync example profiles
        }
        OkHttpClient client = new OkHttpClient();

        Log.d(TAG, "Syncing profile: " + profile.getName());
        //http://www.kevinersoy.com/addProfile.php?profile_ID=globalProfileId&user_ID=GUID&profile_name=profileName&profile_intensity=profileIntensity&profile_delay=profileDelay
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("www.kevinersoy.com")
                .appendPath("addProfile.php")
                .appendQueryParameter("user_ID", profile.getGuid() == null ? DataManager.getInstance().getGUID(this) : profile.getGuid())
                .appendQueryParameter("profile_name", profile.getName())
                .appendQueryParameter("profile_intensity", profile.getIntensity())
                .appendQueryParameter("profile_delay", profile.getDelay());
        String url = builder.build().toString();

        //use OkHttp
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            Log.d(TAG, "OkHttp response on upload : " + response.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "OkHttp upload request failed");
        }
    }


    /**
     * Handle action Fetch in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetch(Boolean restoreOnly) throws JSONException {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("www.kevinersoy.com")
                .appendPath("getProfiles.php");
        if (restoreOnly) {
            builder.appendQueryParameter("user_ID", DataManager.getInstance().getGUID(this));
        }

        String url = builder.build().toString();

        //use OkHttp
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        JSONArray array = null;
        try {
            Response response = client.newCall(request).execute();
            array = new JSONArray(response.body().string());

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "OkHttp fetch request failed");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if(array != null) {
            Log.d(TAG, "JSONArray : " + array);

            ProfileDao profileDao = DataManager.getInstance().getDB(getApplicationContext()).profileDao();

            //Loop through JSON objects returned from the server and either insert or update
            //depending if the _ID exists in the database already
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                String name = object.getString("profile_name");
                String intensity = object.getString("profile_intensity");
                String delay = object.getString("profile_delay");
                String GUID = object.getString("user_ID");
                //String profileId = object.getString("profile_ID");

                // Check if profile like this exists, otherwise insert
                if(profileDao.findAndCount(name, intensity, delay) == 0){
                    Profile profile = new Profile(name, intensity, delay, GUID);
                    profileDao.insert(profile);
                    Log.d(TAG, "Inserting profile from server : " + name);
                }
            }
        }

    }

}
