package com.example.ecostayapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecostayapp.R;
import com.example.ecostayapp.ProfileActivity;
import com.example.ecostayapp.models.QuickAction;

import java.util.List;

public class QuickActionsAdapter extends RecyclerView.Adapter<QuickActionsAdapter.QuickActionViewHolder> {

    private List<QuickAction> quickActions;
    private OnQuickActionClickListener listener;

    public interface OnQuickActionClickListener {
        void onQuickActionClick(int tabIndex);
    }

    public QuickActionsAdapter(List<QuickAction> quickActions, OnQuickActionClickListener listener) {
        this.quickActions = quickActions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuickActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quick_action, parent, false);
        return new QuickActionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuickActionViewHolder holder, int position) {
        QuickAction quickAction = quickActions.get(position);
        holder.bind(quickAction);
    }

    @Override
    public int getItemCount() {
        return quickActions.size();
    }

    public class QuickActionViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewIcon;
        private TextView textViewTitle;
        private TextView textViewSubtitle;

        public QuickActionViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewSubtitle = itemView.findViewById(R.id.textViewSubtitle);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    QuickAction quickAction = quickActions.get(position);
                    
                    // Handle Profile navigation differently
                    if (quickAction.getTabIndex() == 0 && quickAction.getTitle().equals("Profile")) {
                        // Navigate to ProfileActivity
                        Intent intent = new Intent(itemView.getContext(), ProfileActivity.class);
                        itemView.getContext().startActivity(intent);
                    } else if (listener != null) {
                        // Navigate to tabs
                        listener.onQuickActionClick(quickAction.getTabIndex());
                    }
                }
            });
        }

        public void bind(QuickAction quickAction) {
            textViewTitle.setText(quickAction.getTitle());
            textViewSubtitle.setText(quickAction.getSubtitle());
            imageViewIcon.setImageResource(quickAction.getIconResId());
        }
    }
}
