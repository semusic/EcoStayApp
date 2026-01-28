package com.example.ecostayapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ecostayapp.adapters.OnboardingAdapter;

import java.util.Arrays;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPagerOnboarding;
    private OnboardingAdapter adapter;
    private List<Integer> onboardingImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding); // Back to original layout

        initViews();
        setupImageSlider();
        setupClickListeners();
    }

    private void initViews() {
        viewPagerOnboarding = findViewById(R.id.viewPagerOnboarding);
        Log.d("OnboardingActivity", "ViewPager2 found: " + (viewPagerOnboarding != null));
        
        // Define onboarding images - using eco-themed images
        onboardingImages = Arrays.asList(
            R.drawable.onboarding_image_1,  // Eco Resort Scene
            R.drawable.onboarding_image_2,  // Sustainable Living
            R.drawable.onboarding_image_3,  // Eco Activities
            R.drawable.onboarding_image_4   // Welcome to EcoStay
        );
        
        Log.d("OnboardingActivity", "Images loaded: " + onboardingImages.toString());
    }

    private void setupImageSlider() {
        Log.d("OnboardingActivity", "Setting up image slider with " + onboardingImages.size() + " images");
        
        if (viewPagerOnboarding != null) {
            adapter = new OnboardingAdapter(onboardingImages);
            viewPagerOnboarding.setAdapter(adapter);
            
            Log.d("OnboardingActivity", "Adapter set on ViewPager2");
            
            // Add page change listener to update dots
            viewPagerOnboarding.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    Log.d("OnboardingActivity", "Page selected: " + position);
                    updatePaginationDots(position);
                }
            });
        } else {
            Log.e("OnboardingActivity", "ViewPager2 is null!");
        }
        
        // Set initial dot state
        updatePaginationDots(0);
    }

    private void setupClickListeners() {
        findViewById(R.id.btnCreateAccount).setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        findViewById(R.id.txtSignIn).setOnClickListener(v -> {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        });
    }

    private void updatePaginationDots(int currentPage) {
        // Update pagination dots based on current page
        View dot1 = findViewById(R.id.dot1);
        View dot2 = findViewById(R.id.dot2);
        View dot3 = findViewById(R.id.dot3);
        View dot4 = findViewById(R.id.dot4);

        dot1.setBackgroundResource(currentPage == 0 ? R.drawable.pagination_dot_active : R.drawable.pagination_dot_inactive);
        dot2.setBackgroundResource(currentPage == 1 ? R.drawable.pagination_dot_active : R.drawable.pagination_dot_inactive);
        dot3.setBackgroundResource(currentPage == 2 ? R.drawable.pagination_dot_active : R.drawable.pagination_dot_inactive);
        dot4.setBackgroundResource(currentPage == 3 ? R.drawable.pagination_dot_active : R.drawable.pagination_dot_inactive);
    }
}
