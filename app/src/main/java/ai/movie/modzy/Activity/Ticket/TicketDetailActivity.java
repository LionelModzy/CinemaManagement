package ai.movie.modzy.Activity.Ticket;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import ai.movie.modzy.Model.BookingModel;
import ai.movie.modzy.R;

public class TicketDetailActivity extends AppCompatActivity {
    private TextView tvMovieTitle, tvCinema, tvShowtime,
            tvSeats, tvTotalPrice, tvBookingTime,
            tvFoodDetails, tvPaymentStatus;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        // Ánh xạ view
        tvMovieTitle = findViewById(R.id.tv_movie_title);
        tvCinema = findViewById(R.id.tv_cinema);
        tvShowtime = findViewById(R.id.tv_showtime);
        tvSeats = findViewById(R.id.tv_seats);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        tvBookingTime = findViewById(R.id.tv_booking_time);
        tvFoodDetails = findViewById(R.id.tv_food_details);
        tvPaymentStatus = findViewById(R.id.tv_payment_status);

        db = FirebaseFirestore.getInstance();

        String bookingId = getIntent().getStringExtra("booking_id");
        if (bookingId != null) {
            loadBookingDetails(bookingId);
        }
    }

    private void loadBookingDetails(String bookingId) {
        db.collection("bookings").document(bookingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        BookingModel booking = documentSnapshot.toObject(BookingModel.class);
                        if (booking != null) {
                            displayBookingDetails(booking);
                        }
                    }
                });
    }

    private void displayBookingDetails(BookingModel booking) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        // Hiển thị thông tin cơ bản
        tvMovieTitle.setText(booking.getMovieTitle());
        tvCinema.setText("Rạp: " + booking.getCinemaName());
        tvSeats.setText("Ghế: " + String.join(", ", booking.getSeats()));
        tvTotalPrice.setText("Tổng tiền: " + formatCurrency(booking.getTotalPrice()));
        tvBookingTime.setText("Thời gian đặt: " + dateFormat.format(booking.getBookingTime().toDate()));
        tvPaymentStatus.setText("Trạng thái: " + booking.getPaymentStatus());

        if (booking.getShowtime() != null) {
            tvShowtime.setText("Suất chiếu: " + dateFormat.format(booking.getShowtime().toDate()));
        }

        // Hiển thị thông tin đồ ăn
        if (booking.getFoods() != null && !booking.getFoods().isEmpty()) {
            StringBuilder foodDetails = new StringBuilder("Đồ ăn/uống:\n");
            for (Map<String, Object> food : booking.getFoods()) {
                String name = (String) food.get("name");
                long quantity = (long) food.get("quantity");
                long price = (long) food.get("price");

                foodDetails.append("- ")
                        .append(name)
                        .append(" (x").append(quantity).append(") - ")
                        .append(formatCurrency((int)(price * quantity)))
                        .append("\n");
            }
            tvFoodDetails.setText(foodDetails.toString());
        } else {
            tvFoodDetails.setText("Không có đồ ăn/uống");
        }
    }

    private String formatCurrency(int amount) {
        return String.format("%,d", amount).replace(',', '.') + "đ";
    }
}