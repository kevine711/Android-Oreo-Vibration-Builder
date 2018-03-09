package com.kevinersoy.androidoreovibrationbuilder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by kevinersoy on 3/8/18.
 */

public class ProfileRecyclerAdapter extends RecyclerView.Adapter<ProfileRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private final List<ProfileInfo> mProfiles;
    private final LayoutInflater mLayoutInflater;

    public ProfileRecyclerAdapter(Context context, List<ProfileInfo> profiles) {
        mContext = context;
        mProfiles = profiles;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mLayoutInflater.inflate(R.layout.item_profile_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ProfileInfo profile = mProfiles.get(position);
        holder.mTextName.setText(profile.getName());
        holder.mTextIntensity.setText(profile.getIntensity());
        holder.mTextDelay.setText(profile.getDelay());
        holder.mCurrentPosition = position;
    }

    @Override
    public int getItemCount() {
        return mProfiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //using inner class to define ViewHolder since it will only be used with this Adapter
        public final TextView mTextName;
        public final TextView mTextIntensity;
        public final TextView mTextDelay;
        public int mCurrentPosition;

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
                    intent.putExtra(VibrationProfile.PROFILE_POSITION, mCurrentPosition);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
