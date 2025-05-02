package ai.movie.modzy.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import ai.movie.modzy.Activity.Booking.AddShowtimeActivity;
import ai.movie.modzy.Adapter.ShowtimeAdapter;
import ai.movie.modzy.Model.Showtime;
import ai.movie.modzy.R;

public class ScheduleFragment extends Fragment {
    private RecyclerView rvShowtimes;
    private ShowtimeAdapter adapter;
    private List<Showtime> showtimeList;
    private FirebaseFirestore db;
//    private Button btnAddShowtime;
    private String role = "user"; // mặc định
private FloatingActionButton btnAddShowtime;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_schedule_frament, container, false);

        rvShowtimes = view.findViewById(R.id.rvShowtimes);
        btnAddShowtime = view.findViewById(R.id.btnAddShowtime);
        rvShowtimes.setLayoutManager(new LinearLayoutManager(getContext()));
        db = FirebaseFirestore.getInstance();

        // Lấy role từ SharedPreferences
        SharedPreferences prefs = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        role = prefs.getString("user_role", "user");

        if (role.equals("admin")) {
            btnAddShowtime.setVisibility(View.VISIBLE);
        }

        btnAddShowtime.setOnClickListener(v -> {
            // Mở Activity thêm suất chiếu
            startActivity(new Intent(getContext(), AddShowtimeActivity.class));
        });

        showtimeList = new ArrayList<>();
        adapter = new ShowtimeAdapter(getContext(), showtimeList, role);
        rvShowtimes.setAdapter(adapter);

        loadShowtimesFromFirestore();

        return view;
    }

    private void loadShowtimesFromFirestore() {
        db.collection("showtimes").get().addOnSuccessListener(queryDocumentSnapshots -> {
            showtimeList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Showtime showtime = doc.toObject(Showtime.class);
                showtime.setId(doc.getId()); // để dùng cho sửa/xóa
                showtimeList.add(showtime);
            }
            adapter.notifyDataSetChanged();
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        loadShowtimesFromFirestore();
    }
}
