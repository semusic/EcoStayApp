package com.example.ecostayapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ecostayapp.fragments.HomeFragment;
import com.example.ecostayapp.fragments.RoomsFragment;
import com.example.ecostayapp.fragments.ActivitiesFragment;
import com.example.ecostayapp.fragments.BookingsFragment;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout navHome, navRooms, navActivities, navBookings;
    private ImageView btnMenu;
    private TextView txtHome, txtRooms, txtActivities, txtBookings;
    private FirebaseAuth mAuth;

    private int currentNavItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupFirebase();
        setupViewPager();
        setupClickListeners();
        updateNavigationUI(0);
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        navHome = findViewById(R.id.navHome);
        navRooms = findViewById(R.id.navRooms);
        navActivities = findViewById(R.id.navActivities);
        navBookings = findViewById(R.id.navBookings);
        btnMenu = findViewById(R.id.btnMenu);

        txtHome = findViewById(R.id.textHome);
        txtRooms = findViewById(R.id.textRooms);
        txtActivities = findViewById(R.id.textActivities);
        txtBookings = findViewById(R.id.textBookings);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        adapter.addFragment(new HomeFragment());
        adapter.addFragment(new RoomsFragment());
        adapter.addFragment(new ActivitiesFragment());
        adapter.addFragment(new BookingsFragment());

        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false); // Disable swipe

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateNavigationUI(position);
            }
        });
    }

    private void setupClickListeners() {
        navHome.setOnClickListener(v -> {
            viewPager.setCurrentItem(0, false);
            updateNavigationUI(0);
        });

        navRooms.setOnClickListener(v -> {
            viewPager.setCurrentItem(1, false);
            updateNavigationUI(1);
        });

        navActivities.setOnClickListener(v -> {
            viewPager.setCurrentItem(2, false);
            updateNavigationUI(2);
        });

        navBookings.setOnClickListener(v -> {
            viewPager.setCurrentItem(3, false);
            updateNavigationUI(3);
        });

                btnMenu.setOnClickListener(v -> {
                    showMenu(v);
                });
    }

    private void updateNavigationUI(int position) {
        currentNavItem = position;

        // Reset all navigation items
        resetNavigationItems();

        // Highlight selected item with colored icons
        switch (position) {
            case 0:
                ((ImageView)findViewById(R.id.iconHome)).setColorFilter(ContextCompat.getColor(this, R.color.primary_color));
                txtHome.setTextColor(ContextCompat.getColor(this, R.color.primary_color));
                break;
            case 1:
                ((ImageView)findViewById(R.id.iconRooms)).setColorFilter(ContextCompat.getColor(this, R.color.primary_color));
                txtRooms.setTextColor(ContextCompat.getColor(this, R.color.primary_color));
                break;
            case 2:
                ((ImageView)findViewById(R.id.iconActivities)).setColorFilter(ContextCompat.getColor(this, R.color.primary_color));
                txtActivities.setTextColor(ContextCompat.getColor(this, R.color.primary_color));
                break;
            case 3:
                ((ImageView)findViewById(R.id.iconBookings)).setColorFilter(ContextCompat.getColor(this, R.color.primary_color));
                txtBookings.setTextColor(ContextCompat.getColor(this, R.color.primary_color));
                break;
        }
    }

    private void resetNavigationItems() {
        // Reset all icons to gray/inactive color
        ((ImageView)findViewById(R.id.iconHome)).setColorFilter(ContextCompat.getColor(this, R.color.text_secondary));
        ((ImageView)findViewById(R.id.iconRooms)).setColorFilter(ContextCompat.getColor(this, R.color.text_secondary));
        ((ImageView)findViewById(R.id.iconActivities)).setColorFilter(ContextCompat.getColor(this, R.color.text_secondary));
        ((ImageView)findViewById(R.id.iconBookings)).setColorFilter(ContextCompat.getColor(this, R.color.text_secondary));

        // Reset all text colors to gray
        txtHome.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        txtRooms.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        txtActivities.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        txtBookings.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
    }

    // Public method to allow fragments to change tabs
    public void setCurrentTab(int position) {
        viewPager.setCurrentItem(position, true);
        updateNavigationUI(position);
    }

    private void showMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());
        
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                
                if (itemId == R.id.menu_profile) {
                    // Navigate to Profile
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    return true;
                } else if (itemId == R.id.menu_notifications) {
                    // Navigate to Notifications
                    startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
                    return true;
                } else if (itemId == R.id.menu_logout) {
                    // Logout user
                    logoutUser();
                    return true;
                }
                return false;
            }
        });
        
        popupMenu.show();
    }


    private void logoutUser() {
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            
            // Navigate back to SignIn activity
            Intent intent = new Intent(this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }
}