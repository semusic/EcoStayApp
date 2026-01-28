package com.example.ecostayapp.adapters;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecostayapp.R;
import com.example.ecostayapp.models.Room;
import com.example.ecostayapp.utils.ImageLoader;

import java.util.List;

public class AdminRoomsAdapter extends RecyclerView.Adapter<AdminRoomsAdapter.RoomViewHolder> {

    private List<Room> rooms;
    private OnRoomActionListener listener;

    public interface OnRoomActionListener {
        void onEdit(Room room);
        void onDelete(Room room);
        void onToggleAvailability(Room room);
    }

    public AdminRoomsAdapter(List<Room> rooms, OnRoomActionListener listener) {
        this.rooms = rooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_room, parent, false);
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
        private ImageView imageView, btnMoreOptions;
        private TextView textViewName, textViewPrice, textViewStatus, textViewGuests;
        private Button btnToggleAvailability;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewRoom);
            textViewName = itemView.findViewById(R.id.textViewRoomName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewGuests = itemView.findViewById(R.id.textViewGuests);
            btnToggleAvailability = itemView.findViewById(R.id.btnToggleAvailability);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
        }

        public void bind(Room room) {
            textViewName.setText(room.getName());
            // Convert USD to LKR for display
            double priceLKR = room.getPricePerNight() * 325;
            textViewPrice.setText("LKR " + String.format("%.0f", priceLKR) + "/night");
            textViewGuests.setText("Max " + room.getMaxGuests() + " guests");
            
            // Set status
            if (room.isAvailable()) {
                textViewStatus.setText("Available");
                textViewStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.success_color));
                btnToggleAvailability.setText("Mark Unavailable");
            } else {
                textViewStatus.setText("Unavailable");
                textViewStatus.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.error_color));
                btnToggleAvailability.setText("Mark Available");
            }

            // Load image using ImageLoader utility (supports local files and URLs)
            ImageLoader.loadRoomImage(itemView.getContext(), room.getImageUrl(), imageView);

            // Toggle availability click listener
            btnToggleAvailability.setOnClickListener(v -> listener.onToggleAvailability(room));

            // More options (3 dots) click listener - shows dropdown menu
            btnMoreOptions.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(itemView.getContext(), btnMoreOptions);
                popupMenu.getMenuInflater().inflate(R.menu.admin_room_options, popupMenu.getMenu());
                
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        
                        if (itemId == R.id.menu_edit) {
                            listener.onEdit(room);
                            return true;
                        } else if (itemId == R.id.menu_delete) {
                            listener.onDelete(room);
                            return true;
                        }
                        return false;
                    }
                });
                
                popupMenu.show();
            });
        }
    }
}

