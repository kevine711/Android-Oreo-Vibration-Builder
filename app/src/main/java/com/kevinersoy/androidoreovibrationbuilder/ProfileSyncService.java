package com.kevinersoy.androidoreovibrationbuilder;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import static com.kevinersoy.androidoreovibrationbuilder.VibrationBuilderProviderContract.*;

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
                handleActionFetch(restoreOnly);
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

        while(cursor.moveToNext()) {
            String profileName = cursor.getString(profileNamePos);
            String profileDelay = cursor.getString(profileDelayPos);
            String profileIntensity = cursor.getString(profileIntensityPos);
            int currentProfileId = cursor.getInt(profileIdPos);
            String globalProfileId = GUID + currentProfileId;
            //http://www.kevinersoy.com/addProfile.php?profile_ID=globalProfileId&user_ID=GUID&profile_name=profileName&profile_intensity=profileIntensity&profile_delay=profileDelay
            //use OkHttp
        }
        cursor.close();



        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Fetch in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetch(Boolean restoreOnly) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
