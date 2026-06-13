package com.example.healthsync.frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.healthsync.R;
import com.example.healthsync.frontend.models.CommunityActivity;
import java.util.List;

public class CommunityFeedAdapter extends RecyclerView.Adapter<CommunityFeedAdapter.ViewHolder> {
    private List<CommunityActivity> activities;

    public CommunityFeedAdapter(List<CommunityActivity> activities) {
        this.activities = activities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_feed, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CommunityActivity activity = activities.get(position);
        holder.tvName.setText(activity.getUserName());
        holder.tvActivity.setText(activity.getActivityText());
        holder.tvTime.setText("Just now"); // Simplified
    }

    @Override
    public int getItemCount() { return activities.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvActivity, tvTime;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvActivity = itemView.findViewById(R.id.tv_activity_text);
            tvTime = itemView.findViewById(R.id.tv_timestamp);
        }
    }
}
