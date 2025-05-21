package ai.movie.modzy.Activity.Booking;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.movie.modzy.Activity.Food.ChooseFoodDialog;
import ai.movie.modzy.Model.Food;
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
    private CheckBox cbOrderFood;
    private Button btnChooseFood;
//    private List<Food> selectedFoods = new ArrayList<>();
    private Map<Food, Integer> selectedFoods = new HashMap<>();
    private int totalPrice = 0;
    private TextView tvCinemaName, tvMovieTitle, tvShowtime, tvSeatsCount, tvPricePerSeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_movie);

        gridSeats = findViewById(R.id.grid_seats);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnConfirm = findViewById(R.id.btn_confirm_booking);
        tvCinemaName = findViewById(R.id.tv_cinema_name);
        tvMovieTitle = findViewById(R.id.tv_movie_title);
        tvShowtime = findViewById(R.id.tv_time);
        tvSeatsCount = findViewById(R.id.tv_seats_count);
        tvPricePerSeat = findViewById(R.id.tv_price);

        db = FirebaseFirestore.getInstance();

        showtimeId = getIntent().getStringExtra("showtime_id");

        if (showtimeId != null) {
            loadMovieInfo(showtimeId);
            loadBookedSeats(showtimeId);
        }
        cbOrderFood = findViewById(R.id.cb_order_food);
        btnChooseFood = findViewById(R.id.btn_choose_food);

        cbOrderFood.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnChooseFood.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnChooseFood.setOnClickListener(v -> {
            ChooseFoodDialog dialog = new ChooseFoodDialog(this, newSelectedFoods -> {
                this.selectedFoods.clear();
                this.selectedFoods.putAll(newSelectedFoods);
                updateTotalPrice();
                updateSelectedFoodsUI(); // Thêm dòng này
            });

            Map<Food, Integer> copyMap = new HashMap<>(selectedFoods);
            dialog.setSelectedFoods(copyMap);

            dialog.setOnFoodTotalChangedListener(tempFoods -> {
                this.selectedFoods.clear();
                this.selectedFoods.putAll(tempFoods);
                updateTotalPrice();
                updateSelectedFoodsUI(); // Thêm dòng này
            });

            dialog.show();
        });






        btnConfirm.setOnClickListener(v -> confirmBooking());
    }
    private void loadMovieInfo(String showtimeId) {
        Log.d("BookingMovie", "Loading movie info for showtime: " + showtimeId);

        db.collection("showtimes").document(showtimeId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("BookingMovie", "Showtime data: " + documentSnapshot.getData());

                        // Lấy thông tin suất chiếu
                        Long movieIdLong = documentSnapshot.getLong("movieID");
                        if (movieIdLong != null) {
                            String movieIdStr = String.valueOf(movieIdLong);
                            Log.d("BookingMovie", "Movie ID from showtime: " + movieIdStr);

                            // Truy vấn phim
                            db.collection("movies").document(movieIdStr).get()
                                    .addOnSuccessListener(movieDoc -> {
                                        if (movieDoc.exists()) {
                                            String movieTitle = movieDoc.getString("title");
                                            Log.d("BookingMovie", "Movie title found: " + movieTitle);
                                            tvMovieTitle.setText(movieTitle);
                                        } else {
                                            Log.d("BookingMovie", "Movie document not found");
                                            tvMovieTitle.setText("Movie not found");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("BookingMovie", "Error loading movie", e);
                                        tvMovieTitle.setText("Failed to load movie");
                                    });
                        } else {
                            Log.d("BookingMovie", "movieID field is null or not a number");
                        }

                        // Phần còn lại giữ nguyên...
                        String cinemaName = documentSnapshot.getString("cinema");
                        Timestamp timestamp = documentSnapshot.getTimestamp("datetime");

                        if (timestamp != null) {
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            String timeStr = timeFormat.format(timestamp.toDate());
                            String dateStr = dateFormat.format(timestamp.toDate());
                            tvShowtime.setText("Thời gian: " + timeStr + " - " + dateStr);
                        }

                        tvCinemaName.setText(cinemaName);

                        Long price = documentSnapshot.getLong("price");
                        if (price != null) {
                            seatPrice = price.intValue();
                            tvPricePerSeat.setText("Giá vé: " + formatCurrency(seatPrice) + "/seat");
                        }
                    } else {
                        Log.d("BookingMovie", "Showtime document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("BookingMovie", "Error loading showtime", e);
                });
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
        // Thêm header số ghế
        for (int i = 1; i <= 8; i++) {
            TextView header = new TextView(this);
            header.setText(String.valueOf(i));
            header.setGravity(Gravity.CENTER);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 100;
            params.height = 50;
            params.columnSpec = GridLayout.spec(i-1);
            header.setLayoutParams(params);
            gridSeats.addView(header);
        }

        String[] rows = {"A", "B", "C", "D", "E"};
        for (int rowIdx = 0; rowIdx < rows.length; rowIdx++) {
            // Thêm tên hàng
            TextView rowHeader = new TextView(this);
            rowHeader.setText(rows[rowIdx]);
            rowHeader.setGravity(Gravity.CENTER);
            GridLayout.LayoutParams rowParams = new GridLayout.LayoutParams();
            rowParams.width = 50;
            rowParams.height = 100;
            rowParams.rowSpec = GridLayout.spec(rowIdx+1);
            rowHeader.setLayoutParams(rowParams);
            gridSeats.addView(rowHeader);

            for (int col = 1; col <= 8; col++) {
                String seat = rows[rowIdx] + col;
                ImageButton seatButton = new ImageButton(this);

                // Đặt icon tùy theo trạng thái ghế
                if (bookedSeats.contains(seat)) {
                    seatButton.setImageResource(R.drawable.ic_seat_booked);
                    seatButton.setEnabled(false);
                } else {
                    seatButton.setImageResource(R.drawable.ic_seat_available);
                }

                seatButton.setBackgroundColor(Color.TRANSPARENT);
                seatButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
                seatButton.setPadding(8, 8, 8, 8);
                seatButton.setTag(seat);

                seatButton.setOnClickListener(v -> {
                    String clickedSeat = (String) v.getTag();
                    if (selectedSeats.contains(clickedSeat)) {
                        selectedSeats.remove(clickedSeat);
                        ((ImageButton)v).setImageResource(R.drawable.ic_seat_available);
                    } else {
                        selectedSeats.add(clickedSeat);
                        ((ImageButton)v).setImageResource(R.drawable.ic_seat_selected);
                    }
                    updateTotalPrice();
                });

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 100;
                params.height = 100;
                params.rowSpec = GridLayout.spec(rowIdx+1);
                params.columnSpec = GridLayout.spec(col);
                params.setMargins(4, 4, 4, 4);
                seatButton.setLayoutParams(params);

                gridSeats.addView(seatButton);
            }
        }
    }

    private String formatCurrency(int amount) {
        return String.format("%,d", amount).replace(',', '.') + "đ";
    }

    private void updateTotalPrice() {
        // Cập nhật số ghế
        tvSeatsCount.setText("Seats: " + selectedSeats.size());

        int totalSeatPrice = seatPrice * selectedSeats.size();
        int totalFoodPrice = 0;

        for (Map.Entry<Food, Integer> entry : selectedFoods.entrySet()) {
            if (entry.getValue() > 0) {
                totalFoodPrice += entry.getKey().getPrice() * entry.getValue();
            }
        }

        int total = totalSeatPrice + totalFoodPrice;
        tvTotalPrice.setText("Tổng tiền vé: " + formatCurrency(totalSeatPrice) +
                "\nTổng tiền món: " + formatCurrency(totalFoodPrice) +
                "\nTổng thanh toán: " + formatCurrency(total));
    }
    private void updateSelectedFoodsUI() {
        TextView tvSelectedFoods = findViewById(R.id.tv_selected_foods);
        StringBuilder sb = new StringBuilder("Món đã chọn:\n");

        boolean hasSelectedFood = false;
        for (Map.Entry<Food, Integer> entry : selectedFoods.entrySet()) {
            if (entry.getValue() > 0) {
                hasSelectedFood = true;
                sb.append("- ")
                        .append(entry.getKey().getName())
                        .append(" (x")
                        .append(entry.getValue())
                        .append(")\n");
            }
        }

        if (!hasSelectedFood) {
            sb.append("Chưa chọn món nào");
        }

        tvSelectedFoods.setText(sb.toString());
    }






    private void confirmBooking() {
        if (selectedSeats.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ghế", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        int totalSeats = selectedSeats.size();
        int totalSeatPrice = seatPrice * totalSeats;
        int totalFoodPrice = 0;
        for (Map.Entry<Food, Integer> entry : selectedFoods.entrySet()) {
            if (entry.getValue() > 0) {
                totalFoodPrice += entry.getKey().getPrice() * entry.getValue();
            }
        }
        totalPrice = totalSeatPrice + totalFoodPrice;

        List<Map<String, Object>> foodListMap = new ArrayList<>();
        for (Map.Entry<Food, Integer> entry : selectedFoods.entrySet()) {
            if (entry.getValue() > 0) {
                Map<String, Object> foodMap = new HashMap<>();
                foodMap.put("id", entry.getKey().getId());
                foodMap.put("name", entry.getKey().getName());
                foodMap.put("price", (long) entry.getKey().getPrice());
                foodMap.put("quantity", (long) entry.getValue());
                foodListMap.add(foodMap);
            }
        }

        Map<String, Object> booking = new HashMap<>();
        booking.put("userId", userId);
        booking.put("showtimeId", showtimeId);
        booking.put("seats", selectedSeats);
        booking.put("totalPrice", totalPrice);
        booking.put("paymentStatus", "pending");
        booking.put("paymentMethod", "momo");
        booking.put("bookingTime", Timestamp.now());
        booking.put("foods", foodListMap);

        // Thêm booking và lấy ID
        db.collection("bookings").add(booking)
                .addOnSuccessListener(documentReference -> {
                    String bookingId = documentReference.getId();

                    // Thêm bookingId vào dữ liệu booking
                    booking.put("booking_id", bookingId);

                    // Cập nhật document với bookingId
                    documentReference.set(booking)
                            .addOnSuccessListener(aVoid -> {
                                // Cập nhật thông tin ghế đã đặt
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

                                                                // Chuyển sang BillActivity với bookingId
                                                                Intent intent = new Intent(BookingMovieActivity.this, BillActivity.class);
                                                                intent.putExtra("booking_id", bookingId);
                                                                intent.putExtra("showtime_id", showtimeId);
                                                                intent.putExtra("totalPrice", totalPrice);
                                                                intent.putExtra("selectedSeats", new ArrayList<>(selectedSeats));
                                                                intent.putExtra("foodList", (ArrayList<Map<String, Object>>) foodListMap);
                                                                intent.putExtra("totalSeatPrice", totalSeatPrice);
                                                                startActivity(intent);
                                                            });
                                                }
                                            }
                                        });
                            });
                });
    }
}
