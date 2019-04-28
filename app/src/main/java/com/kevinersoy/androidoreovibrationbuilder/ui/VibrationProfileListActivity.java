package com.kevinersoy.androidoreovibrationbuilder.ui;

import android.app.LoaderManager;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.kevinersoy.androidoreovibrationbuilder.BuildConfig;
import com.kevinersoy.androidoreovibrationbuilder.DataManager;
import com.kevinersoy.androidoreovibrationbuilder.ProfileSyncService;
import com.kevinersoy.androidoreovibrationbuilder.R;
import com.kevinersoy.androidoreovibrationbuilder.db.room.LocalProfileDataSource;
import com.kevinersoy.androidoreovibrationbuilder.provider.VibrationBuilderProviderContract.Profiles;
import com.kevinersoy.androidoreovibrationbuilder.db.VibrationProfileBuilderOpenHelper;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class VibrationProfileListActivity extends AppCompatActivity {

    public static final String TAG = VibrationProfileListActivity.class.getSimpleName();

    private ProfileRecyclerAdapter mProfileRecyclerAdapter;

    // Adding architecture components
    private ViewModelFactory mViewModelFactory;
    private ProfileListViewModel mProfileListViewModel;
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration_profile_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        enableStrictMode();

        // Architecture added
        mViewModelFactory = new ViewModelFactory(
                new LocalProfileDataSource(
                        DataManager.getInstance().getDB(getApplicationContext()).profileDao()
                )
        );
        mProfileListViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ProfileListViewModel.class);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> startActivity(new Intent(VibrationProfileListActivity.this, VibrationProfileActivity.class)));

        initializeContent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDisposable.add(mProfileListViewModel.getProfiles()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(profiles -> mProfileRecyclerAdapter.updateList(profiles),
                        throwable -> Log.e(TAG, "Unable to update list", throwable)));
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDisposable.clear();
    }

    private void enableStrictMode() {
        if(BuildConfig.DEBUG){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d("VibrationProfileListActivity", "Options item selected");
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_upload) {
            upload();
        } else if (id == R.id.action_restore){
            restore();
        }
        return super.onOptionsItemSelected(item);
    }

    private void restore() {
        //Use intent service to restore all profiles from the server
        Toast.makeText(this, getString(R.string.restoringprofilesfromserver), Toast.LENGTH_SHORT).show();
        ProfileSyncService.startActionFetch(this, true);
    }

    private void upload() {
        //Use intent service to upload all profiles to server
        Toast.makeText(this, getString(R.string.uploadingtoserver), Toast.LENGTH_SHORT).show();
        ProfileSyncService.startActionUpload(this, ProfileSyncService.ALL_PROFILES);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initializeContent() {
        //Set up RecyclerView with a layout manager
        final RecyclerView recyclerProfiles = (RecyclerView) findViewById(R.id.list_profiles);
        final LinearLayoutManager profilesLayoutManager = new LinearLayoutManager(this);
        recyclerProfiles.setLayoutManager(profilesLayoutManager);

        //Set field for our recycler adapter passing a null cursor and set this adapter for
        //our RecyclerView
        //Cursor will get set when we loadProfiles() in onResume()
        mProfileRecyclerAdapter = new ProfileRecyclerAdapter(this, null);
        recyclerProfiles.setAdapter(mProfileRecyclerAdapter);

    }


}

