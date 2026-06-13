package com.example.healthsync.frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.healthsync.R;
import com.example.healthsync.frontend.models.MoodEntry;
import java.util.List;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.ViewHolder> {
    private List<MoodEntry> moods;

    public MoodAdapter(List<MoodEntry> moods) {
        this.moods = moods;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mood_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvEmoji.setText(moods.get(position).getEmoji());
    }

    @Override
    public int getItemCount() { return moods.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji;
        ViewHolder(View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tv_mood_emoji);
        }
    }
}
