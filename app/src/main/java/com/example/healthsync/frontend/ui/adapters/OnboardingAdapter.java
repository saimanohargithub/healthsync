package com.example.healthsync.frontend.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.healthsync.R;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private final List<OnboardingItem> items;

    public OnboardingAdapter(List<OnboardingItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OnboardingViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        private final LottieAnimationView lottieIllustration;
        private final TextView tvTitle;
        private final TextView tvDescription;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            lottieIllustration = itemView.findViewById(R.id.lottie_illustration);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
        }

        void bind(OnboardingItem item) {
            lottieIllustration.setAnimation(item.lottieRawRes);
            tvTitle.setText(item.title);
            tvDescription.setText(item.description);
        }
    }

    public static class OnboardingItem {
        public final String title;
        public final String description;
        public final int lottieRawRes;

        public OnboardingItem(String title, String description, int lottieRawRes) {
            this.title = title;
            this.description = description;
            this.lottieRawRes = lottieRawRes;
        }
    }
}
