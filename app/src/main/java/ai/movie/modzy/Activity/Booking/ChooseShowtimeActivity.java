package ai.movie.modzy.Activity.Booking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.*;

import ai.movie.modzy.Model.Showtime;
import ai.movie.modzy.R;

public class ChooseShowtimeActivity extends AppCompatActivity {

    private Spinner spinnerCinema, spinnerDate, spinnerTime;
    private Button btnContinue;
    private FirebaseFirestore db;
    private int movieId;

    private List<Showtime> allShowtimes = new ArrayList<>();
    private Showtime selectedShowtime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_showtime);

        spinnerCinema = findViewById(R.id.spinner_cinema);
        spinnerDate = findViewById(R.id.spinner_date);
        spinnerTime = findViewById(R.id.spinner_time);
        btnContinue = findViewById(R.id.btn_continue);

        db = FirebaseFirestore.getInstance();
        movieId = getIntent().getIntExtra("movie_id", -1);

        if (movieId == -1) {
            Toast.makeText(this, "Không tìm thấy phim!", Toast.LENGTH_SHORT).show();
            finish();
        }

        loadShowtimes();

        btnContinue.setOnClickListener(v -> {
            if (selectedShowtime != null) {
                Intent intent = new Intent(this, BookingMovieActivity.class);
                intent.putExtra("showtime_id", selectedShowtime.getId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Vui lòng chọn đủ thông tin!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadShowtimes() {
        db.collection("showtimes").whereEqualTo("movieID", movieId).get()
                .addOnSuccessListener(query -> {
                    allShowtimes.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Showtime s = doc.toObject(Showtime.class);
                        s.setId(doc.getId());
                        allShowtimes.add(s);
                    }
                    setupCinemaSpinner();
                });
    }

    private void setupCinemaSpinner() {
        Set<String> cinemas = new HashSet<>();
        for (Showtime s : allShowtimes) {
            cinemas.add(s.getCinema());
        }
        List<String> cinemaList = new ArrayList<>(cinemas);
        Collections.sort(cinemaList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, cinemaList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCinema.setAdapter(adapter);


        spinnerCinema.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(String selectedCinema) {
                updateDateSpinner(selectedCinema);
            }
        });
    }

    private void updateDateSpinner(String selectedCinema) {
        Set<String> dates = new TreeSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (Showtime s : allShowtimes) {
            if (s.getCinema().equals(selectedCinema)) {
                dates.add(sdf.format(s.getDatetime().toDate()));
            }
        }

//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(dates));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, new ArrayList<>(dates));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerCinema.setAdapter(adapter);

        spinnerDate.setAdapter(adapter);

        spinnerDate.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(String selectedDate) {
                updateTimeSpinner(selectedCinema, selectedDate);
            }
        });
    }

    private void updateTimeSpinner(String selectedCinema, String selectedDate) {
        List<String> times = new ArrayList<>();
        SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());

        Map<String, Showtime> timeMap = new HashMap<>();

        for (Showtime s : allShowtimes) {
            String dateStr = sdfDate.format(s.getDatetime().toDate());
            if (s.getCinema().equals(selectedCinema) && dateStr.equals(selectedDate)) {
                String timeStr = sdfTime.format(s.getDatetime().toDate());
                times.add(timeStr);
                timeMap.put(timeStr, s);
            }
        }

//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, times);
//
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, times);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(adapter);
        spinnerTime.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(String selectedTime) {
                selectedShowtime = timeMap.get(selectedTime);
            }
        });
    }
}
