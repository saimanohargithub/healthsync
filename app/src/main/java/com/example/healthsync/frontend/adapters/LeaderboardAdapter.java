package com.example.healthsync.frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.healthsync.R;
import com.example.healthsync.frontend.models.UserRank;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<UserRank> ranks;

    public LeaderboardAdapter(List<UserRank> ranks) {
        this.ranks = ranks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserRank rank = ranks.get(position);
        holder.tvRank.setText("#" + rank.getRank());
        holder.tvName.setText(rank.getName());
        holder.tvPoints.setText(rank.getPoints() + " pts");
        holder.tvChallengeProgress.setText(rank.getChallengeProgress());
    }

    @Override
    public int getItemCount() { return ranks.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvPoints, tvChallengeProgress;
        ViewHolder(View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPoints = itemView.findViewById(R.id.tv_points);
            tvChallengeProgress = itemView.findViewById(R.id.tv_challenge_progress);
        }
    }
}
