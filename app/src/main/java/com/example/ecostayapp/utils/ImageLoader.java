package com.example.ecostayapp.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Utility class to load images from local storage or URLs
 */
public class ImageLoader {

    /**
     * Load image from local file path or URL
     * Automatically detects if the path is a local file or URL
     */
    public static void loadImage(Context context, String imagePath, ImageView imageView, int placeholderResId) {
        if (imagePath == null || imagePath.isEmpty()) {
            imageView.setImageResource(placeholderResId);
            return;
        }

        // Check if it's a local file path
        if (imagePath.startsWith("/")) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                // Load from local file
                Glide.with(context)
                        .load(imageFile)
                        .placeholder(placeholderResId)
                        .error(placeholderResId)
                        .into(imageView);
            } else {
                // File doesn't exist, show placeholder
                imageView.setImageResource(placeholderResId);
            }
        } else if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            // Load from URL (backward compatibility)
            Glide.with(context)
                    .load(imagePath)
                    .placeholder(placeholderResId)
                    .error(placeholderResId)
                    .into(imageView);
        } else {
            // Unknown format, show placeholder
            imageView.setImageResource(placeholderResId);
        }
    }

    /**
     * Load room image
     */
    public static void loadRoomImage(Context context, String imagePath, ImageView imageView) {
        loadImage(context, imagePath, imageView, com.example.ecostayapp.R.drawable.room_placeholder);
    }

    /**
     * Load activity image
     */
    public static void loadActivityImage(Context context, String imagePath, ImageView imageView) {
        loadImage(context, imagePath, imageView, com.example.ecostayapp.R.drawable.activity_placeholder);
    }

    /**
     * Load eco initiative image
     */
    public static void loadEcoImage(Context context, String imagePath, ImageView imageView) {
        loadImage(context, imagePath, imageView, com.example.ecostayapp.R.drawable.eco_placeholder);
    }
}








