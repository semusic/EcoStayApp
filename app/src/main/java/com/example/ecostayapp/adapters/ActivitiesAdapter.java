package com.example.ecostayapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecostayapp.R;
import com.example.ecostayapp.models.Activity;

import java.util.List;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder> {

    private List<Activity> activities;
    private OnActivityClickListener listener;

    public interface OnActivityClickListener {
        void onActivityClick(Activity activity);
    }

    public ActivitiesAdapter(List<Activity> activities, OnActivityClickListener listener) {
        this.activities = activities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activities.get(position);
        holder.bind(activity);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    class ActivityViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textViewName;
        private TextView textViewDescription;
        private TextView textViewPrice;
        private TextView textViewDuration;
        private TextView textViewDifficulty;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewActivity);
            textViewName = itemView.findViewById(R.id.textViewActivityName);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewDuration = itemView.findViewById(R.id.textViewDuration);
            textViewDifficulty = itemView.findViewById(R.id.textViewDifficulty);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActivityClick(activities.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Activity activity) {
            textViewName.setText(activity.getName());
            textViewDescription.setText(activity.getDescription());
            // Convert USD to LKR for display
            double priceLKR = activity.getPrice() * 325;
            textViewPrice.setText("LKR " + String.format("%.0f", priceLKR));
            textViewDuration.setText(activity.getDuration());
            textViewDifficulty.setText(activity.getDifficulty());

            // Load image using Glide
            if (activity.getImageUrl() != null && !activity.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(activity.getImageUrl())
                        .placeholder(R.drawable.activity_placeholder)
                        .error(R.drawable.activity_placeholder)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.activity_placeholder);
            }
        }
    }
}
