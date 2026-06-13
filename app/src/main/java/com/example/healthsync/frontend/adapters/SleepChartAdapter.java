package com.example.healthsync.frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.healthsync.R;
import com.example.healthsync.frontend.models.SleepDay;
import java.util.List;

public class SleepChartAdapter extends RecyclerView.Adapter<SleepChartAdapter.ViewHolder> {
    private List<SleepDay> sleepDays;

    public SleepChartAdapter(List<SleepDay> sleepDays) {
        this.sleepDays = sleepDays;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sleep_bar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SleepDay day = sleepDays.get(position);
        holder.tvDay.setText(day.getDay());
        
        // Set bar height based on hours (max 10h)
        float weight = day.getHours() / 10f;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                holder.viewBar.getLayoutParams().width,
                0,
                weight
        );
        holder.viewBar.setLayoutParams(params);
    }

    @Override
    public int getItemCount() { return sleepDays.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewBar;
        TextView tvDay;
        ViewHolder(View itemView) {
            super(itemView);
            viewBar = itemView.findViewById(R.id.view_bar);
            tvDay = itemView.findViewById(R.id.tv_day);
        }
    }
}
