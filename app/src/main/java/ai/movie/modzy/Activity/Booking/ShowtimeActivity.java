package ai.movie.modzy.Activity.Booking;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import ai.movie.modzy.Adapter.ShowtimeAdapter;
import ai.movie.modzy.Model.Showtime;
import ai.movie.modzy.R;

public class ShowtimeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ShowtimeAdapter adapter;
    private List<Showtime> showtimeList = new ArrayList<>();
    private FirebaseFirestore db;
    private int movieId;
    private String role;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtime);
// Lấy role từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        role = prefs.getString("user_role", "user"); // Mặc định là user
        recyclerView = findViewById(R.id.recyclerView);
        db = FirebaseFirestore.getInstance();
        adapter = new ShowtimeAdapter(this, showtimeList, role); // ✅ đúng 3 tham số

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        movieId = getIntent().getIntExtra("movie_id", -1);

        if (movieId != -1) {
            loadShowtimes(movieId);
        }
    }

    private void loadShowtimes(int movieId) {
        db.collection("showtimes").whereEqualTo("movieID", movieId).get()
                .addOnSuccessListener(query -> {
                    showtimeList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Showtime showtime = doc.toObject(Showtime.class);
                        showtimeList.add(showtime);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
