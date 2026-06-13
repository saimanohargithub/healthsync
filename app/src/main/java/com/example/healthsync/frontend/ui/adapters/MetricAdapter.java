package com.example.healthsync.frontend.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.healthsync.databinding.ItemMetricBinding;
import com.example.healthsync.frontend.utils.MockData;
import java.util.List;

public class MetricAdapter extends RecyclerView.Adapter<MetricAdapter.ViewHolder> {

    private final List<MockData.HealthMetric> metrics;

    public MetricAdapter(List<MockData.HealthMetric> metrics) {
        this.metrics = metrics;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemMetricBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MockData.HealthMetric metric = metrics.get(position);
        holder.binding.tvMetricTitle.setText(metric.title);
        holder.binding.tvMetricValue.setText(metric.value);
        holder.binding.progressMetric.setProgress(metric.progress);
        holder.binding.progressMetric.setIndicatorColor(metric.color);
    }

    @Override
    public int getItemCount() {
        return metrics.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemMetricBinding binding;
        ViewHolder(ItemMetricBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
