package com.kevinersoy.androidoreovibrationbuilder;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class VibrationProfileList extends AppCompatActivity {

    private ArrayAdapter<ProfileInfo> mAdapterProfiles;

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
        mAdapterProfiles.notifyDataSetChanged();
    }

    private void initializeContent() {
        final ListView listProfiles = (ListView)findViewById(R.id.list_profiles);

        //getInstance initializes the example profiles for now
        List<ProfileInfo> profiles = DataManager.getInstance().getProfiles();
        mAdapterProfiles = new ArrayAdapter<ProfileInfo>(this,android.R.layout.simple_list_item_1, profiles);

        listProfiles.setAdapter(mAdapterProfiles);

        listProfiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(VibrationProfileList.this, VibrationProfile.class);
                intent.putExtra(VibrationProfile.PROFILE_POSITION, position);
                startActivity(intent);
            }
        });

    }


}

