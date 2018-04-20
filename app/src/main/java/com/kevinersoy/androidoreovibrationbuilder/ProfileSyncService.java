package com.kevinersoy.androidoreovibrationbuilder;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.kevinersoy.androidoreovibrationbuilder.VibrationBuilderProviderContract.Profiles;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * This class will be used to upload profiles to the server, restore my profiles from the server
 * and (later) fetch other peoples profiles from the server
 */
public class ProfileSyncService extends IntentService {

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
                    Log.e("ProfileSyncService", "JSON parse failed");
                }
            }
        }
    }

    /**
     * Handle action Upload in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpload(String profileId) {
        String[] columns = {
                Profiles.COLUMN_PROFILE_NAME,
                Profiles.COLUMN_PROFILE_DELAY,
                Profiles.COLUMN_PROFILE_INTENSITY,
                Profiles._ID
        };
        String selection = null;
        String[] selectionArgs = null;
        if(!profileId.equals(ALL_PROFILES)){
            selection = Profiles._ID + " = ? ";
            selectionArgs = new String[] {profileId};
        }
        Cursor cursor = getContentResolver().query(Profiles.CONTENT_URI, columns, selection, selectionArgs, null);
        int profileNamePos = cursor.getColumnIndex(Profiles.COLUMN_PROFILE_NAME);
        int profileDelayPos = cursor.getColumnIndex(Profiles.COLUMN_PROFILE_DELAY);
        int profileIntensityPos = cursor.getColumnIndex(Profiles.COLUMN_PROFILE_INTENSITY);
        int profileIdPos = cursor.getColumnIndex(Profiles._ID);

        String GUID = DataManager.getInstance().getGUID(this);

        OkHttpClient client = new OkHttpClient();

        while(cursor.moveToNext()) {
            String profileName = cursor.getString(profileNamePos);
            String profileDelay = cursor.getString(profileDelayPos);
            String profileIntensity = cursor.getString(profileIntensityPos);
            int currentProfileId = cursor.getInt(profileIdPos);
            String globalProfileId = GUID + currentProfileId;

            //Skip upload if Example Profile
            if("Example1Example2Example3".contains(profileName)) {
                Log.d("ProfileSyncService", "Found Example Profile");
                continue;
            }

            Log.d("ProfileSyncService", "Syncing profile: " + profileName);

            //http://www.kevinersoy.com/addProfile.php?profile_ID=globalProfileId&user_ID=GUID&profile_name=profileName&profile_intensity=profileIntensity&profile_delay=profileDelay
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("www.kevinersoy.com")
                    .appendPath("addProfile.php")
                    .appendQueryParameter("profile_ID", globalProfileId)
                    .appendQueryParameter("user_ID", GUID)
                    .appendQueryParameter("profile_name", profileName)
                    .appendQueryParameter("profile_intensity", profileIntensity)
                    .appendQueryParameter("profile_delay", profileDelay);
            String url = builder.build().toString();

            //use OkHttp
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                Log.d("ProfileSyncService", "OkHttp response on upload : " + response.toString());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ProfileSyncService", "OkHttp upload request failed");
            }

        }
        cursor.close();
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
            Log.e("ProfileSyncService", "OkHttp fetch request failed");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if(array != null) {
            Log.d("ProfileSyncService", "JSONArray : " + array);

            //Loop through JSON objects returned from the server and either insert or update
            //depending if the _ID exists in the database already
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                String name = object.getString("profile_name");
                String intensity = object.getString("profile_intensity");
                String delay = object.getString("profile_delay");
                String GUID = object.getString("user_ID");
                String profileId = object.getString("profile_ID");
                String localId = parseLocalId(GUID, profileId);

                String[] profileColumns = {
                        Profiles.COLUMN_PROFILE_NAME,
                        Profiles.COLUMN_PROFILE_INTENSITY,
                        Profiles.COLUMN_PROFILE_DELAY,
                        Profiles._ID
                };
                Uri profileUri = ContentUris.withAppendedId(Profiles.CONTENT_URI, Integer.parseInt(localId));
                try {
                    Cursor cursor = getContentResolver().query(profileUri, profileColumns, null, null, null);
                    if (cursor != null && cursor.getCount() > 0){
                        //found item in table with this index, update it
                        final ContentValues values = new ContentValues();
                        values.put(Profiles.COLUMN_PROFILE_NAME, name);
                        values.put(Profiles.COLUMN_PROFILE_INTENSITY, intensity);
                        values.put(Profiles.COLUMN_PROFILE_DELAY, delay);
                        getContentResolver().update(profileUri, values, null, null);
                    } else {
                        //no item with this index found, insert it.
                        final ContentValues values = new ContentValues();
                        values.put(Profiles.COLUMN_PROFILE_NAME, name);
                        values.put(Profiles.COLUMN_PROFILE_INTENSITY, intensity);
                        values.put(Profiles.COLUMN_PROFILE_DELAY, delay);
                        values.put(Profiles._ID, localId);
                        getContentResolver().insert(Profiles.CONTENT_URI, values);
                    }

                    if (cursor != null)
                        cursor.close();
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        }

    }

    private String parseLocalId(String guid, String profileId) {
        //profile ID is the GUID appended with the local database _ID
        //extracting local database _ID
        return profileId.substring(Math.min(guid.length(), profileId.length()));
    }
}
