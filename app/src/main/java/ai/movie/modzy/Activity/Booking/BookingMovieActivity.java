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

    private String showtimeId;
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

        showtimeId = getIntent().getStringExtra("showtime_id");

        if (showtimeId != null) {
            loadBookedSeats(showtimeId);
        }


        btnConfirm.setOnClickListener(v -> confirmBooking());
    }

    private void loadBookedSeats(String showtimeId) {
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

    private String formatCurrency(int amount) {
        return String.format("%,d", amount).replace(',', '.') + "đ";
    }

    private void updateTotalPrice() {
        int total = seatPrice * selectedSeats.size();
        tvTotalPrice.setText("Tổng tiền: " + formatCurrency(total));
    }


    private void confirmBooking() {
        if (selectedSeats.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ghế", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // đảm bảo đã login

        int totalSeats = selectedSeats.size();
        int totalPrice = seatPrice * totalSeats;

        Map<String, Object> booking = new HashMap<>();
        booking.put("userId", userId);
        booking.put("showtimeId", showtimeId);
        booking.put("seats", selectedSeats);
        booking.put("totalPrice", totalPrice);
        booking.put("paymentStatus", "pending");
        booking.put("paymentMethod", "momo");
        booking.put("bookingTime", Timestamp.now());

        db.collection("bookings").add(booking)
                .addOnSuccessListener(doc -> {
                    // Lấy document showtime để cập nhật availableSeats
                    db.collection("showtimes").document(showtimeId).get()
                            .addOnSuccessListener(snapshot -> {
                                if (snapshot.exists()) {
                                    Long currentAvailableSeats = snapshot.getLong("availableSeats");
                                    if (currentAvailableSeats != null) {
                                        int updatedSeats = currentAvailableSeats.intValue() - totalSeats;

                                        if (updatedSeats < 0) updatedSeats = 0;

                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("bookedSeats", FieldValue.arrayUnion(selectedSeats.toArray()));
                                        updates.put("availableSeats", updatedSeats);

                                        db.collection("showtimes").document(showtimeId)
                                                .update(updates)
                                                .addOnSuccessListener(unused -> {
                                                    Toast.makeText(this, "Đặt vé thành công!", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                });
                                    }
                                }
                            });
                });
    }

}
