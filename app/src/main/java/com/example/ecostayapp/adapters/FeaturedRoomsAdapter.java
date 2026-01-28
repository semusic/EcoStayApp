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
import com.example.ecostayapp.models.Room;
import com.example.ecostayapp.utils.ImageLoader;

import java.util.List;

public class FeaturedRoomsAdapter extends RecyclerView.Adapter<FeaturedRoomsAdapter.RoomViewHolder> {

    private List<Room> rooms;
    private OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }

    public FeaturedRoomsAdapter(List<Room> rooms, OnRoomClickListener listener) {
        this.rooms = rooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_featured_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.bind(room);
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    class RoomViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textViewName;
        private TextView textViewPrice;
        private TextView textViewGuests;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewRoom);
            textViewName = itemView.findViewById(R.id.textViewRoomName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewGuests = itemView.findViewById(R.id.textViewGuests);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRoomClick(rooms.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Room room) {
            textViewName.setText(room.getName());
            // Convert USD to LKR for display
            double priceLKR = room.getPricePerNight() * 325;
            textViewPrice.setText("LKR " + String.format("%.0f", priceLKR) + "/night");
            textViewGuests.setText(room.getMaxGuests() + " guests");

            // Load image using Glide
            // Load image using ImageLoader utility (supports local files and URLs)
            ImageLoader.loadRoomImage(itemView.getContext(), room.getImageUrl(), imageView);
        }
    }
}
