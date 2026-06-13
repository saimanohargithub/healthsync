package com.example.healthsync.frontend.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthsync.R;
import com.example.healthsync.backend.data.local.MealEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealLogAdapter extends RecyclerView.Adapter<MealLogAdapter.ViewHolder> {
    private List<MealEntity> meals = new ArrayList<>();

    public void setMeals(List<MealEntity> meals) {
        this.meals = meals;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealEntity meal = meals.get(position);
        holder.tvName.setText(meal.getMealName());
        holder.tvKcal.setText(String.format(Locale.getDefault(), "%.0f kcal", meal.getCalories()));
        
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        holder.tvTime.setText("Completed at " + sdf.format(new Date(meal.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvKcal, tvTime;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_meal_name);
            tvKcal = itemView.findViewById(R.id.tv_meal_kcal);
            tvTime = itemView.findViewById(R.id.tv_meal_time);
        }
    }
}
