package com.kevinersoy.androidoreovibrationbuilder;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kevinersoy.androidoreovibrationbuilder.VibrationProfileBuilderDatabaseContract.ProfileInfoEntry;

/**
 * Created by kevinersoy on 3/8/18.
 */

public class ProfileRecyclerAdapter extends RecyclerView.Adapter<ProfileRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private Cursor mCursor;
    private final LayoutInflater mLayoutInflater;
    private int mProfileNamePos;
    private int mProfileIntensityPos;
    private int mProfileDelayPos;
    private int mIdPos;

    public ProfileRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mLayoutInflater = LayoutInflater.from(mContext);
        populateColumnPositions();
    }

    private void populateColumnPositions() {
        if (mCursor == null)
            return;
        //Get column index from mCursor
        mProfileNamePos = mCursor.getColumnIndex(ProfileInfoEntry.COLUMN_PROFILE_NAME);
        mProfileIntensityPos = mCursor.getColumnIndex(ProfileInfoEntry.COLUMN_PROFILE_INTENSITY);
        mProfileDelayPos = mCursor.getColumnIndex(ProfileInfoEntry.COLUMN_PROFILE_DELAY);
        mIdPos = mCursor.getColumnIndex(ProfileInfoEntry._ID);
    }

    public void changeCursor(Cursor cursor) {
        if (mCursor != null)
            mCursor.close();
        mCursor = cursor;
        populateColumnPositions();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.item_profile_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String name = mCursor.getString(mProfileNamePos);
        String intensity = mCursor.getString(mProfileIntensityPos);
        String delay = mCursor.getString(mProfileDelayPos);
        int id = mCursor.getInt(mIdPos);
        holder.mTextName.setText(name);
        holder.mTextIntensity.setText(intensity);
        holder.mTextDelay.setText(delay);
        holder.mId = id;
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //using inner class to define ViewHolder since it will only be used with this Adapter
        public final TextView mTextName;
        public final TextView mTextIntensity;
        public final TextView mTextDelay;
        public int mId;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextName = (TextView)itemView.findViewById(R.id.text_name);
            mTextIntensity = (TextView)itemView.findViewById(R.id.text_intensity);
            mTextDelay = (TextView)itemView.findViewById(R.id.text_delay);

            //setting onClickListener here on the parent view because no child view is clickable
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, VibrationProfile.class);
                    intent.putExtra(VibrationProfile.PROFILE_ID, mId);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
