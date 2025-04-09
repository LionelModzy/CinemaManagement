package ai.movie.modzy.Activity.Booking;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import ai.movie.modzy.R;

public class AddShowtimeActivity extends AppCompatActivity {

    EditText edtId, edtMovieId, edtCinema, edtRoom, edtPrice, edtAvailableSeats;
    TextView tvDatetime;
    Button btnSave;

    FirebaseFirestore db;
    Calendar selectedCalendar = Calendar.getInstance(); // ngày giờ chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_showtime);

        edtMovieId = findViewById(R.id.edt_movie_id);
        edtCinema = findViewById(R.id.edt_cinema);
        edtRoom = findViewById(R.id.edt_room);
        edtPrice = findViewById(R.id.edt_price);
        edtAvailableSeats = findViewById(R.id.edt_available_seats);
        tvDatetime = findViewById(R.id.tv_datetime);
        btnSave = findViewById(R.id.btn_save);
        db = FirebaseFirestore.getInstance();

        tvDatetime.setOnClickListener(v -> showDateTimePicker());
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String role = prefs.getString("user_role", "user");

        if (!role.equals("admin")) {
            Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity nếu không phải admin
            return;
        }
        String editingId = getIntent().getStringExtra("showtime_id");
        if (editingId != null) {
            loadShowtimeForEditing(editingId);
        }

        btnSave.setOnClickListener(v -> saveShowtime());
    }

    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedCalendar.set(Calendar.MINUTE, minute);
                                tvDatetime.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(selectedCalendar.getTime()));
                            }, 18, 0, true);
                    timePickerDialog.show();
                }, 2025, 3, 10);
        datePickerDialog.show();
    }

    private void saveShowtime() {
        // Không cần lấy ID nhập tay nữa → ẩn hoặc bỏ edtId trong XML nếu muốn

        int movieId = Integer.parseInt(edtMovieId.getText().toString());
        String cinema = edtCinema.getText().toString();
        String room = edtRoom.getText().toString();
        int price = Integer.parseInt(edtPrice.getText().toString());
        int availableSeats = Integer.parseInt(edtAvailableSeats.getText().toString());

        // Tạo document mới có id tự sinh
        db.collection("showtimes").add(new HashMap<>()) // placeholder để lấy ID
                .addOnSuccessListener(docRef -> {
                    String generatedId = docRef.getId(); // Lấy ID tự sinh từ Firestore

                    Map<String, Object> data = new HashMap<>();
                    data.put("id", generatedId); // lưu id dạng String
                    data.put("movieID", movieId);
                    data.put("cinema", cinema);
                    data.put("room", room);
                    data.put("price", price);
                    data.put("availableSeats", availableSeats);
                    data.put("datetime", new Timestamp(selectedCalendar.getTime()));
                    data.put("status", "available");
                    data.put("bookedSeats", new ArrayList<String>());

                    // Ghi đè lại document với ID vừa tạo
                    db.collection("showtimes").document(generatedId).set(data)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Đã thêm suất chiếu", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                });
    }
    private void loadShowtimeForEditing(String id) {
        db.collection("showtimes").document(id).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                edtMovieId.setText(String.valueOf(doc.getLong("movieID")));
                edtCinema.setText(doc.getString("cinema"));
                edtRoom.setText(doc.getString("room"));
                edtPrice.setText(String.valueOf(doc.getLong("price")));
                edtAvailableSeats.setText(String.valueOf(doc.getLong("availableSeats")));
                Timestamp timestamp = doc.getTimestamp("datetime");
                if (timestamp != null) {
                    selectedCalendar.setTime(timestamp.toDate());
                    tvDatetime.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(timestamp.toDate()));
                }

                btnSave.setOnClickListener(v -> {
                    updateShowtime(id);
                });
            }
        });
    }
    private void updateShowtime(String id) {
        int movieId = Integer.parseInt(edtMovieId.getText().toString());
        String cinema = edtCinema.getText().toString();
        String room = edtRoom.getText().toString();
        int price = Integer.parseInt(edtPrice.getText().toString());
        int availableSeats = Integer.parseInt(edtAvailableSeats.getText().toString());

        Map<String, Object> data = new HashMap<>();
        data.put("movieID", movieId);
        data.put("cinema", cinema);
        data.put("room", room);
        data.put("price", price);
        data.put("availableSeats", availableSeats);
        data.put("datetime", new Timestamp(selectedCalendar.getTime()));

        db.collection("showtimes").document(id).update(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã cập nhật suất chiếu", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }



}
