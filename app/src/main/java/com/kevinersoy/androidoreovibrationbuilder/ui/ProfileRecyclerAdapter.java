package com.kevinersoy.androidoreovibrationbuilder.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.kevinersoy.androidoreovibrationbuilder.R;
import com.kevinersoy.androidoreovibrationbuilder.db.VibrationProfileBuilderDatabaseContract.ProfileInfoEntry;
import com.kevinersoy.androidoreovibrationbuilder.db.room.Profile;

import java.util.List;

/**
 * Created by kevinersoy on 3/8/18.
 * This class is our custom adapter for the RecyclerView from VibrationProfileListActivity activity
 * We'll make use of a passed cursor (from database query) to populate our view holders
 * Nested ViewHolder class extends RecyclerView.ViewHolder and sets the onClickListeners for each
 * view.
 * Notify super class when dataset changed (when new cursor is supplied)
 */

public class ProfileRecyclerAdapter extends RecyclerView.Adapter<ProfileRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private List<Profile> mProfiles;
    private final LayoutInflater mLayoutInflater;
    private int lastPosition = -1;

    public ProfileRecyclerAdapter(Context context, List<Profile> profiles) {
        //Constructor - set fields and get column indices
        mContext = context;
        mProfiles = profiles;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void updateList(List<Profile> profiles){
        mProfiles = profiles;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //When a view holder item is created, inflate that view and return the  a new ViewHolder
        // instance with the View we inflated.
        View itemView = mLayoutInflater.inflate(R.layout.item_profile_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //Binding data to the ViewHolder
        /*
            Set holder fields with data from the list
         */
        holder.mTextName.setText(mProfiles.get(position).getName());
        holder.mTextIntensity.setText(mProfiles.get(position).getIntensity());
        holder.mTextDelay.setText(mProfiles.get(position).getDelay());
        holder.mId = mProfiles.get(position).getId();

        setAnimation(holder.itemView, position);
    }

    private void setAnimation(View viewToAnimate, int position) {
        viewToAnimate.startAnimation(
                AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top));
        lastPosition = position;
    }

    @Override
    public int getItemCount() {
        return mProfiles == null ? 0 : mProfiles.size();
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
                    Intent intent = new Intent(mContext, VibrationProfileActivity.class);
                    intent.putExtra(VibrationProfileActivity.PROFILE_ID, mId);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
