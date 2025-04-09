package ai.movie.modzy.Activity.Booking;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.movie.modzy.R;

public class BookingMovieActivity extends AppCompatActivity {
    private GridLayout gridSeats;
    private TextView tvTotalPrice;
    private Button btnConfirm;
    private FirebaseFirestore db;

    private int showtimeId;
    private int seatPrice = 85000; // giá mặc định (có thể lấy từ Firestore)
    private List<String> selectedSeats = new ArrayList<>();
    private List<String> bookedSeats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_movie);

        gridSeats = findViewById(R.id.grid_seats);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnConfirm = findViewById(R.id.btn_confirm_booking);
        db = FirebaseFirestore.getInstance();

        showtimeId = getIntent().getIntExtra("showtime_id", -1);

        if (showtimeId != -1) {
            loadBookedSeats(showtimeId);
        }

        btnConfirm.setOnClickListener(v -> confirmBooking());
    }

    private void loadBookedSeats(int showtimeId) {
        db.collection("showtimes").document(String.valueOf(showtimeId)).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        bookedSeats = (List<String>) snapshot.get("bookedSeats");
                        if (bookedSeats == null) bookedSeats = new ArrayList<>();
                        generateSeats();
                    }
                });
    }

    private void generateSeats() {
        String[] rows = {"A", "B", "C", "D", "E"};
        for (String row : rows) {
            for (int i = 1; i <= 8; i++) {
                String seat = row + i;
                Button seatButton = new Button(this);
                seatButton.setText(seat);
                seatButton.setPadding(8, 8, 8, 8);
                seatButton.setBackgroundColor(bookedSeats.contains(seat) ? Color.GRAY : Color.LTGRAY);
                seatButton.setEnabled(!bookedSeats.contains(seat));

                seatButton.setOnClickListener(v -> {
                    if (selectedSeats.contains(seat)) {
                        selectedSeats.remove(seat);
                        seatButton.setBackgroundColor(Color.LTGRAY);
                    } else {
                        selectedSeats.add(seat);
                        seatButton.setBackgroundColor(Color.GREEN);
                    }
                    updateTotalPrice();
                });

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.setMargins(8, 8, 8, 8);
                params.width = 100;
                params.height = 100;
                seatButton.setLayoutParams(params);

                gridSeats.addView(seatButton);
            }
        }
    }

    private void updateTotalPrice() {
        int total = seatPrice * selectedSeats.size();
        tvTotalPrice.setText("Tổng tiền: " + total + "đ");
    }

    private void confirmBooking() {
        if (selectedSeats.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ghế", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // cần login trước

        Map<String, Object> booking = new HashMap<>();
        booking.put("userId", userId);
        booking.put("showtimeId", showtimeId);
        booking.put("seats", TextUtils.join(", ", selectedSeats));
        booking.put("totalPrice", seatPrice * selectedSeats.size());
        booking.put("paymentStatus", "pending");
        booking.put("paymentMethod", "momo"); // hoặc chọn
        booking.put("bookingTime", Timestamp.now());

        db.collection("bookings").add(booking)
                .addOnSuccessListener(doc -> {
                    db.collection("showtimes").document(String.valueOf(showtimeId))
                            .update("bookedSeats", FieldValue.arrayUnion(selectedSeats.toArray()))
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Đặt vé thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                });
    }
}
