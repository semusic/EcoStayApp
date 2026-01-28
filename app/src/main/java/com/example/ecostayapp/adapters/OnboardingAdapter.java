package com.example.ecostayapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostayapp.R;

import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {

    private List<Integer> imageResources;

    public OnboardingAdapter(List<Integer> imageResources) {
        this.imageResources = imageResources;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding_slide, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        // Set the main image
        int imageResource = imageResources.get(position);
        holder.imageViewOnboarding.setImageResource(imageResource);
        holder.imageViewOnboarding.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return imageResources.size();
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewOnboarding;

        OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewOnboarding = itemView.findViewById(R.id.imageViewOnboarding);
        }
    }
}
