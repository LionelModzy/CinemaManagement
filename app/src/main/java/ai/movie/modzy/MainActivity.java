package ai.movie.modzy;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Movie;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

import ai.movie.modzy.Activity.Account.Login_Activity;
import ai.movie.modzy.Activity.Movie.AddMovieActivity;
import ai.movie.modzy.Activity.Ticket.ScanQRActivity;
import ai.movie.modzy.Adapter.MovieAdapter;
import ai.movie.modzy.Fragment.FoodFragment;
import ai.movie.modzy.Fragment.HomeFragment;
import ai.movie.modzy.Fragment.ManageAccountFragment;
import ai.movie.modzy.Fragment.MovieFragment;
import ai.movie.modzy.Fragment.ProfileFragment;
import ai.movie.modzy.Fragment.ScheduleFragment;
import ai.movie.modzy.Fragment.StatisticFragment;
import ai.movie.modzy.Fragment.TicketFragment;
import ai.movie.modzy.Model.Movies;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private String role = "user"; // Default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer); // Layout chứa cả drawer + bottom nav

        // Nhận role từ Login/Register
        role = getIntent().getStringExtra("role");

        // Ánh xạ view
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // Thiết lập Drawer Toggle (hamburger icon)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Xử lý Navigation Drawer
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_movie) {
                MovieFragment movieFragment = new MovieFragment();
                Bundle bundle = new Bundle();
                bundle.putString("role", role);
                movieFragment.setArguments(bundle);
                loadFragment(movieFragment, " Quản lí phim");


        } else if (itemId == R.id.nav_schedules) {
                loadFragment(new ScheduleFragment(), "Quản lí suất chiếu");
            } else if (itemId == R.id.nav_account) {
                if (role.equals("admin")) {
                    loadFragment(new ManageAccountFragment(), "Quản lý tài khoản");
                } else {
                    Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
                }


            } else if (itemId == R.id.nav_statistics) {
                loadFragment(new StatisticFragment(), "Thống kê");
            } else if (itemId == R.id.nav_ticketqr) {
                Intent intent = new Intent(this, ScanQRActivity.class);
                startActivity(intent);
            }

         else if (itemId == R.id.nav_foods) {
                if (role.equals("admin")) {
                    loadFragment(new FoodFragment(), "Quản lý bắp nước");
                } else {
                    Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
                }


        } else if (itemId == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, Login_Activity.class));
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

// Xử lý Bottom Navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment(), "Trang chủ");
                return true;
            } else if (itemId == R.id.nav_movies) {
                loadFragment(new MovieFragment(), "Phim");
                return true;
            } else if (itemId == R.id.nav_tickets) {
                loadFragment(new TicketFragment(), "Vé");
                return true;
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment(), "Tài khoản");
                return true;
            }

            return false;
        });


        // Mặc định load Fragment đầu tiên
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "Trang chủ");
        }
        // Lấy role từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "user"); // mặc định là user

        // Ẩn menu nếu không phải admin
        if (!role.equals("admin")) {
            navigationView.getMenu().findItem(R.id.nav_movie).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_account).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_schedules).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_ticketqr).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_foods).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_statistics).setVisible(false); // nếu cần
        }
        // Nếu là admin thì ẩn tab "Phim" ở bottom nav
        if (role.equals("admin")) {
            bottomNavigationView.getMenu().findItem(R.id.nav_movies).setVisible(false);
        }

    }

    private void loadFragment(Fragment fragment, String title) {
        Bundle bundle = new Bundle();
        bundle.putString("role", role); // Truyền role cho Fragment
        fragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_content, fragment) // phải đúng ID trong layout
                .commit();

        if (title != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}

