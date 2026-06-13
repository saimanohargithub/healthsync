package com.example.healthsync.frontend.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.healthsync.backend.ui.viewmodels.CommunityViewModel;
import com.example.healthsync.backend.ui.viewmodels.ViewModelFactory;
import com.example.healthsync.frontend.adapters.BadgeAdapter;
import com.example.healthsync.frontend.adapters.CommunityFeedAdapter;
import com.example.healthsync.frontend.adapters.LeaderboardAdapter;
import com.example.healthsync.databinding.FragmentCommunityBinding;
import com.example.healthsync.databinding.ItemActiveChallengeBinding;
import com.example.healthsync.frontend.models.Challenge;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;

public class CommunityFragment extends Fragment {

    private FragmentCommunityBinding binding;
    private CommunityViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCommunityBinding.inflate(inflater, container, false);
        
        ViewModelFactory factory = new ViewModelFactory(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(CommunityViewModel.class);
        
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeViewModel();
        setupDiscoverSection();
    }

    private void observeViewModel() {
        viewModel.getActiveChallenges().observe(getViewLifecycleOwner(), challenges -> {
            Log.d("CHALLENGE_STATE_UPDATE", "Active challenges updated: " + challenges.size());
            updateActiveChallengesUI(challenges);
        });

        viewModel.getCommunityFeed().observe(getViewLifecycleOwner(), activities -> {
            if (activities == null || activities.isEmpty()) {
                binding.rvCommunityFeed.setVisibility(View.GONE);
                binding.tvNoActivity.setVisibility(View.VISIBLE);
            } else {
                binding.rvCommunityFeed.setVisibility(View.VISIBLE);
                binding.tvNoActivity.setVisibility(View.GONE);
                CommunityFeedAdapter adapter = new CommunityFeedAdapter(activities);
                binding.rvCommunityFeed.setAdapter(adapter);
            }
        });

        viewModel.getLeaderboardRanks().observe(getViewLifecycleOwner(), ranks -> {
            if (ranks == null || ranks.isEmpty()) {
                binding.rvLeaderboard.setVisibility(View.GONE);
                binding.tvNoLeaderboard.setVisibility(View.VISIBLE);
            } else {
                binding.rvLeaderboard.setVisibility(View.VISIBLE);
                binding.tvNoLeaderboard.setVisibility(View.GONE);
                LeaderboardAdapter adapter = new LeaderboardAdapter(ranks);
                binding.rvLeaderboard.setAdapter(adapter);
            }
        });

        viewModel.getUserBadges().observe(getViewLifecycleOwner(), badges -> {
            if (badges == null || badges.isEmpty()) {
                binding.rvBadges.setVisibility(View.GONE);
                binding.tvNoBadges.setVisibility(View.VISIBLE);
            } else {
                binding.rvBadges.setVisibility(View.VISIBLE);
                binding.tvNoBadges.setVisibility(View.GONE);
                BadgeAdapter adapter = new BadgeAdapter(badges);
                binding.rvBadges.setAdapter(adapter);
            }
        });

        viewModel.getCommunityStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null && !stats.isEmpty()) {
                binding.gridStats.setVisibility(View.VISIBLE);
                binding.tvNoStats.setVisibility(View.GONE);
                binding.tvTotalCommunitySteps.setText(stats.getOrDefault("steps", "--"));
                binding.tvAvgCommunitySleep.setText(stats.getOrDefault("sleep", "--"));
            } else {
                binding.gridStats.setVisibility(View.GONE);
                binding.tvNoStats.setVisibility(View.VISIBLE);
            }
        });

        viewModel.isStepsJoined().observe(getViewLifecycleOwner(), joined -> {
            if (Boolean.TRUE.equals(joined)) {
                binding.cardDiscover1.setVisibility(View.GONE);
            } else {
                binding.cardDiscover1.setVisibility(View.VISIBLE);
                binding.btnJoin.setText("Join");
                binding.btnJoin.setEnabled(true);
            }
        });

        viewModel.isHydrationJoined().observe(getViewLifecycleOwner(), joined -> {
            if (Boolean.TRUE.equals(joined)) {
                binding.cardDiscover2.setVisibility(View.GONE);
            } else {
                binding.cardDiscover2.setVisibility(View.VISIBLE);
                binding.btnJoinHyd.setText("Join");
                binding.btnJoinHyd.setEnabled(true);
            }
        });

        viewModel.getJoinStatus().observe(getViewLifecycleOwner(), status -> {
            if ("SUCCESS".equals(status)) {
                Snackbar.make(binding.getRoot(), "Challenge Joined!", Snackbar.LENGTH_SHORT).show();
            } else if ("ERROR".equals(status)) {
                Snackbar.make(binding.getRoot(), "Failed to join challenge", Snackbar.LENGTH_SHORT).show();
                // Re-enable buttons on error
                binding.btnJoin.setEnabled(true);
                binding.btnJoin.setText("Join");
                binding.btnJoinHyd.setEnabled(true);
                binding.btnJoinHyd.setText("Join");
            }
        });
    }

    private void setupDiscoverSection() {
        binding.btnJoin.setOnClickListener(v -> {
            binding.btnJoin.setText("Joining...");
            binding.btnJoin.setEnabled(false);
            viewModel.join10kSteps();
        });
        
        binding.btnJoinHyd.setOnClickListener(v -> {
            binding.btnJoinHyd.setText("Joining...");
            binding.btnJoinHyd.setEnabled(false);
            viewModel.joinHydrationHero();
        });
    }

    private void updateActiveChallengesUI(List<Challenge> activeChallenges) {
        if (binding == null) return;
        binding.layoutActiveChallenges.removeAllViews();
        if (activeChallenges == null || activeChallenges.isEmpty()) {
            binding.cardEmptyChallenges.setVisibility(View.VISIBLE);
            binding.tvChallengesTitle.setVisibility(View.GONE);
        } else {
            binding.cardEmptyChallenges.setVisibility(View.GONE);
            binding.tvChallengesTitle.setVisibility(View.VISIBLE);
            for (Challenge c : activeChallenges) {
                ItemActiveChallengeBinding itemBinding = ItemActiveChallengeBinding.inflate(getLayoutInflater(), binding.layoutActiveChallenges, false);
                itemBinding.tvChallengeName.setText(c.getName());
                itemBinding.tvReward.setText(String.format(Locale.getDefault(), "+%d pts", c.getRewardPoints()));
                itemBinding.tvProgressText.setText(String.format(Locale.getDefault(), "%d / %d", c.getCurrentProgress(), c.getGoal()));
                
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                itemBinding.tvJoinedDate.setText("Joined on: " + sdf.format(new java.util.Date(c.getJoinedAt())));

                itemBinding.pbChallenge.setMax(c.getGoal());
                itemBinding.pbChallenge.setProgress(c.getCurrentProgress());
                itemBinding.tvPercentage.setText(String.format(Locale.getDefault(), "%d%%", c.getPercentage()));
                binding.layoutActiveChallenges.addView(itemBinding.getRoot());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
