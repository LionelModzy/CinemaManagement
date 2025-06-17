package ai.movie.modzy.Activity.Booking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.movie.modzy.Activity.Ticket.ScanQRActivity;
import ai.movie.modzy.R;

public class BillActivity extends AppCompatActivity {
    private TextView tvCinemaName, tvMovieTitle, tvShowtime, tvSeats, tvSeatPrice, tvFoodDetails, tvTotalPrice;
    private ImageView ivQRCode;
    private Button btnPayNow;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        db = FirebaseFirestore.getInstance();

        // Khởi tạo view
        tvCinemaName = findViewById(R.id.tv_cinema_name);
        tvMovieTitle = findViewById(R.id.tv_movie_title);
        tvShowtime = findViewById(R.id.tv_showtime);
        tvSeats = findViewById(R.id.tv_seats);
        tvSeatPrice = findViewById(R.id.tv_seat_price);
        tvFoodDetails = findViewById(R.id.tv_food_details);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        ivQRCode = findViewById(R.id.iv_qr_code);
        btnPayNow = findViewById(R.id.btn_pay_now);
// Lấy bookingId từ Intent
        String bookingId = getIntent().getStringExtra("booking_id");
        if (bookingId != null) {
            // Tạo mã QR với bookingId
            generateQRCode(bookingId);

            // Load thông tin booking từ Firestore
            loadBookingInfo(bookingId);
        }
        // Lấy dữ liệu từ Intent
        String showtimeId = getIntent().getStringExtra("showtime_id");
        int totalPrice = getIntent().getIntExtra("totalPrice", 0);
        int totalSeatPrice = getIntent().getIntExtra("totalSeatPrice", 0);
        List<String> selectedSeats = getIntent().getStringArrayListExtra("selectedSeats");
        List<Map<String, Object>> foodList = (List<Map<String, Object>>) getIntent().getSerializableExtra("foodList");

        // Tạo mã QR với đầy đủ thông tin
        generateQRCode(bookingId);

        // Hiển thị thông tin
        if (showtimeId != null) {
            loadShowtimeInfo(showtimeId);
        }

        // Hiển thị ghế đã chọn
        tvSeats.setText("Ghế: " + TextUtils.join(", ", selectedSeats));
        tvSeatPrice.setText("Tiền vé: " + formatCurrency(totalSeatPrice));

        // Hiển thị thông tin đồ ăn - SỬA LỖI Ở ĐÂY
        if (foodList != null && !foodList.isEmpty()) {
            StringBuilder foodDetails = new StringBuilder("Đồ ăn/uống:\n");
            for (Map<String, Object> food : foodList) {
                String name = (String) food.get("name");

                // Xử lý giá cả
                int price = 0;
                Object priceObj = food.get("price");
                if (priceObj instanceof Long) {
                    price = ((Long) priceObj).intValue();
                } else if (priceObj instanceof Double) {
                    price = ((Double) priceObj).intValue();
                }

                // Xử lý số lượng
                int quantity = 0;
                Object quantityObj = food.get("quantity");
                if (quantityObj instanceof Long) {
                    quantity = ((Long) quantityObj).intValue();
                } else if (quantityObj instanceof Double) {
                    quantity = ((Double) quantityObj).intValue();
                }

                foodDetails.append("- ").append(name)
                        .append(" (x").append(quantity).append(") - ")
                        .append(formatCurrency(price * quantity)).append("\n");
            }
            tvFoodDetails.setText(foodDetails.toString());
        } else {
            tvFoodDetails.setText("Không có đồ ăn/uống");
        }

        // Hiển thị tổng tiền
        tvTotalPrice.setText("Tổng thanh toán: " + formatCurrency(totalPrice));

        btnPayNow.setOnClickListener(v -> {
            // Xử lý thanh toán
            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
            finish();
        });
        Button btnScanQR = findViewById(R.id.btn_scan_qr);
        btnScanQR.setOnClickListener(v -> {
            Intent intent = new Intent(BillActivity.this, ScanQRActivity.class);
            startActivity(intent);
        });
    }
    private void loadBookingInfo(String bookingId) {
        db.collection("bookings").document(bookingId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> bookingData = documentSnapshot.getData();

                        // Đảm bảo booking_id được thêm vào dữ liệu
                        if (!bookingData.containsKey("booking_id")) {
                            bookingData.put("booking_id", bookingId);
                        }

                        // Hiển thị thông tin booking
                        displayBookingInfo(bookingData);

                        // Load thông tin suất chiếu
                        if (bookingData.containsKey("showtimeId")) {
                            loadShowtimeInfo(bookingData.get("showtimeId").toString());
                        }
                    }
                });
    }
    private void displayBookingInfo(Map<String, Object> bookingData) {
        // Hiển thị thông tin cơ bản
        tvSeats.setText("Ghế: " + TextUtils.join(", ", (List<String>) bookingData.get("seats")));
        tvTotalPrice.setText("Tổng thanh toán: " + formatCurrency(((Long) bookingData.get("totalPrice")).intValue()));

        // Hiển thị thông tin đồ ăn
        List<Map<String, Object>> foodList = (List<Map<String, Object>>) bookingData.get("foods");
        if (foodList != null && !foodList.isEmpty()) {
            StringBuilder foodDetails = new StringBuilder("Đồ ăn/uống:\n");
            for (Map<String, Object> food : foodList) {
                String name = (String) food.get("name");
                int quantity = ((Long) food.get("quantity")).intValue();
                int price = ((Long) food.get("price")).intValue();

                foodDetails.append("- ")
                        .append(name)
                        .append(" (x").append(quantity).append(") - ")
                        .append(formatCurrency(price * quantity)).append("\n");
            }
            tvFoodDetails.setText(foodDetails.toString());
        } else {
            tvFoodDetails.setText("Không có đồ ăn/uống");
        }
    }
    private void loadShowtimeInfo(String showtimeId) {
        db.collection("showtimes").document(showtimeId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Lấy thông tin rạp
                        String cinemaName = documentSnapshot.getString("cinema");
                        tvCinemaName.setText("Rạp: " + cinemaName);

                        // Lấy thông tin ngày giờ
                        com.google.firebase.Timestamp timestamp = documentSnapshot.getTimestamp("datetime");
                        if (timestamp != null) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            tvShowtime.setText("Suất chiếu: " + dateFormat.format(timestamp.toDate()));
                        }

                        // Lấy thông tin phim
                        Long movieId = documentSnapshot.getLong("movieID");
                        if (movieId != null) {
                            db.collection("movies").document(String.valueOf(movieId)).get()
                                    .addOnSuccessListener(movieDoc -> {
                                        if (movieDoc.exists()) {
                                            String movieTitle = movieDoc.getString("title");
                                            tvMovieTitle.setText("Phim: " + movieTitle);
                                        }
                                    });
                        }
                    }
                });
    }

    private void generateQRCode(String bookingId) {
        try {
            JSONObject ticket = new JSONObject();
            ticket.put("type", "movie_ticket");
            ticket.put("booking_id", bookingId);

            // Lấy dữ liệu từ Intent một cách an toàn
            Intent intent = getIntent();
            if (intent != null) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    if (extras.containsKey("showtime_id")) {
                        ticket.put("showtime_id", extras.getString("showtime_id"));
                    }
                    if (extras.containsKey("userId")) {
                        ticket.put("user_id", extras.getString("userId"));
                    }
                }
            }

            // Tạo mã QR từ chuỗi JSON
            createQRImage(ticket.toString());
        } catch (JSONException e) {
            createQRImage("MOVIE-" + bookingId);
        }
    }

    private void createQRImage(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            ivQRCode.setImageBitmap(bmp);
        } catch (WriterException e) {
            Toast.makeText(this, "Lỗi tạo mã QR", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateSimpleQRCode(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            ivQRCode.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void saveQRCodeInfo(String qrContent) {
        // Lưu vào SharedPreferences để có thể truy cập sau
        SharedPreferences prefs = getSharedPreferences("MovieTickets", MODE_PRIVATE);
        prefs.edit().putString("last_qr_code", qrContent).apply();
    }

    private String formatCurrency(int amount) {
        return String.format("%,d", amount).replace(',', '.') + "đ";
    }
}