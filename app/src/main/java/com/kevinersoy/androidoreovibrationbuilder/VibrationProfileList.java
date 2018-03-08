package com.kevinersoy.androidoreovibrationbuilder;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.List;

public class VibrationProfileList extends AppCompatActivity {
    private ProfileRecyclerAdapter mProfileRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration_profile_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
    protected void onResume() {
        super.onResume();
        mProfileRecyclerAdapter.notifyDataSetChanged();
    }

    private void initializeContent() {

        final RecyclerView recyclerProfiles = (RecyclerView) findViewById(R.id.list_profiles);
        final LinearLayoutManager profilesLayoutManager = new LinearLayoutManager(this);
        recyclerProfiles.setLayoutManager(profilesLayoutManager);

        List<ProfileInfo> profiles = DataManager.getInstance().getProfiles();
        mProfileRecyclerAdapter = new ProfileRecyclerAdapter(this, profiles);
        recyclerProfiles.setAdapter(mProfileRecyclerAdapter);
    }


}

