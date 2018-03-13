package com.kevinersoy.androidoreovibrationbuilder;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.kevinersoy.androidoreovibrationbuilder.VibrationProfileBuilderDatabaseContract.ProfileInfoEntry;

public class VibrationProfileList extends AppCompatActivity {
    private ProfileRecyclerAdapter mProfileRecyclerAdapter;
    private VibrationProfileBuilderOpenHelper mDbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration_profile_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Create database open helper instance
        mDbOpenHelper = new VibrationProfileBuilderOpenHelper(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VibrationProfileList.this, VibrationProfile.class));
            }
        });

        initializeContent();
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfiles();
    }

    private void loadProfiles() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] profileColumns = {
                ProfileInfoEntry.COLUMN_PROFILE_NAME,
                ProfileInfoEntry.COLUMN_PROFILE_INTENSITY,
                ProfileInfoEntry.COLUMN_PROFILE_DELAY,
                ProfileInfoEntry._ID};
        Cursor profileCursor = db.query(ProfileInfoEntry.TABLE_NAME, profileColumns,
                null, null, null, null, null);
        mProfileRecyclerAdapter.changeCursor(profileCursor);
    }

    private void initializeContent() {
        DataManager.loadFromDatabase(mDbOpenHelper);
        final RecyclerView recyclerProfiles = (RecyclerView) findViewById(R.id.list_profiles);
        final LinearLayoutManager profilesLayoutManager = new LinearLayoutManager(this);
        recyclerProfiles.setLayoutManager(profilesLayoutManager);


        mProfileRecyclerAdapter = new ProfileRecyclerAdapter(this, null);
        recyclerProfiles.setAdapter(mProfileRecyclerAdapter);

    }


}

