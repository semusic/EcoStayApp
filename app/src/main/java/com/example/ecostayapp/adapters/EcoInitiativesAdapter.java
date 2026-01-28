package com.example.ecostayapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecostayapp.R;
import com.example.ecostayapp.models.EcoInitiative;

import java.util.List;

public class EcoInitiativesAdapter extends RecyclerView.Adapter<EcoInitiativesAdapter.InitiativeViewHolder> {

    private List<EcoInitiative> initiatives;

    public EcoInitiativesAdapter(List<EcoInitiative> initiatives) {
        this.initiatives = initiatives;
    }

    @NonNull
    @Override
    public InitiativeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_eco_initiative, parent, false);
        return new InitiativeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InitiativeViewHolder holder, int position) {
        EcoInitiative initiative = initiatives.get(position);
        holder.bind(initiative);
    }

    @Override
    public int getItemCount() {
        return initiatives.size();
    }

    class InitiativeViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewDescription;

        public InitiativeViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
        }

        public void bind(EcoInitiative initiative) {
            textViewTitle.setText(initiative.getTitle());
            textViewDescription.setText(initiative.getDescription());
        }
    }
}



