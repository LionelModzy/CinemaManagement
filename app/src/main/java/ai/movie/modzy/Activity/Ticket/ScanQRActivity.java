package ai.movie.modzy.Activity.Ticket;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.movie.modzy.R;

public class ScanQRActivity extends AppCompatActivity {
    private static final int RC_BARCODE_CAPTURE = 9001;
    private TextView tvScanResult;
    private static final int PERMISSION_REQUEST_CAMERA = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        tvScanResult = findViewById(R.id.tv_scan_result);
        Button btnScan = findViewById(R.id.btn_scan);



        btnScan.setOnClickListener(v -> {
            if (isDeviceSupportCamera()) {
                // Sử dụng IntentIntegrator từ thư viện ZXing
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setOrientationLocked(false);
                integrator.setPrompt("Quét mã vé xem phim");
                integrator.setCameraId(0); // Sử dụng camera sau
                integrator.setBeepEnabled(true); // Tiếng bíp khi quét thành công
                integrator.setBarcodeImageEnabled(true);
                integrator.initiateScan();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Xử lý kết quả từ ZXing
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                // Người dùng bấm back
                Toast.makeText(this, "Quét mã QR đã bị hủy", Toast.LENGTH_SHORT).show();
            } else {
                // Xử lý dữ liệu quét được
                processQRContent(result.getContents());
            }
        } else {
            // Không phải kết quả từ ZXing
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

//    private void processQRContent(String qrContent) {
//        try {
//            JSONObject ticketInfo = new JSONObject(qrContent);
//            if (ticketInfo.has("type") && ticketInfo.getString("type").equals("movie_ticket")) {
//                // Là vé hợp lệ
//                displayTicketInfo(ticketInfo);
//            } else {
//                showInvalidQRMessage();
//            }
//        } catch (JSONException e) {
//            // Không phải định dạng JSON của vé
//            showInvalidQRMessage();
//        }
//    }
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp
            } else {
                Toast.makeText(this, "Cần quyền camera để quét mã", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showInvalidQRMessage() {
        new AlertDialog.Builder(this)
                .setTitle("Mã QR không hợp lệ")
                .setMessage("Đây không phải là mã vé xem phim hợp lệ")
                .setPositiveButton("OK", null)
                .show();
    }
    private boolean isDeviceSupportCamera() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        }
        Toast.makeText(this, "Thiết bị không hỗ trợ camera", Toast.LENGTH_SHORT).show();
        return false;
    }

    private void displayTicketInfo(JSONObject ticketInfo) throws JSONException {
        // Tạo layout động để hiển thị đẹp hơn
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("THÔNG TIN VÉ XEM PHIM");
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setGravity(Gravity.CENTER);
        layout.addView(tvTitle);

        // Hiển thị từng thông tin
        addInfoRow(layout, "Mã đặt vé:", ticketInfo.getString("booking_id"));

        if (ticketInfo.has("showtime_id")) {
            addInfoRow(layout, "Suất chiếu:", ticketInfo.getString("showtime_id"));
        }

        // Thêm nút "Xác nhận"
        Button btnConfirm = new Button(this);
        btnConfirm.setText("XÁC NHẬN");
        btnConfirm.setOnClickListener(v -> finish());
        layout.addView(btnConfirm);

        // Hiển thị trong ScrollView
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);

        setContentView(scrollView);
    }

    private void addInfoRow(LinearLayout layout, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTypeface(null, Typeface.BOLD);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.3f));

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.7f));

        row.addView(tvLabel);
        row.addView(tvValue);
        layout.addView(row);
    }

    private String formatCurrency(int amount) {
        return String.format("%,d", amount).replace(',', '.') + "đ";
    }
    private void processQRContent(String qrContent) {
        try {
            JSONObject ticketInfo = new JSONObject(qrContent);
            if (ticketInfo.has("type") && "movie_ticket".equals(ticketInfo.getString("type"))) {
                String bookingId = ticketInfo.optString("booking_id", "");

                if (!bookingId.isEmpty()) {
                    FirebaseFirestore.getInstance().collection("bookings").document(bookingId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    Map<String, Object> bookingData = documentSnapshot.getData();

                                    // Thêm thông tin từ QR code vào bookingData một cách an toàn
                                    try {
                                        if (ticketInfo.has("user_id")) {
                                            bookingData.put("userId", ticketInfo.getString("user_id"));
                                        }
                                        if (ticketInfo.has("showtime_id")) {
                                            bookingData.put("showtimeId", ticketInfo.getString("showtime_id"));
                                        }
                                    } catch (JSONException e) {
                                        // Xử lý lỗi nếu cần
                                    }

                                    displayFullTicketInfo(bookingData);
                                } else {
                                    showInvalidQRMessage();
                                }
                            })
                            .addOnFailureListener(e -> showInvalidQRMessage());
                } else {
                    showInvalidQRMessage();
                }
            } else {
                showInvalidQRMessage();
            }
        } catch (JSONException e) {
            showInvalidQRMessage();
        }
    }

    private void displayFullTicketInfo(Map<String, Object> bookingData) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        // Tiêu đề
        TextView tvTitle = new TextView(this);
        tvTitle.setText("THÔNG TIN VÉ XEM PHIM");
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setGravity(Gravity.CENTER);
        layout.addView(tvTitle);

        // Sử dụng các phương thức helper an toàn
        // Hiển thị booking_id
        addInfoRow(layout, "Mã đặt vé:", bookingData.get("booking_id") != null ?
                bookingData.get("booking_id").toString() : "Không có thông tin");
//        addInfoRow(layout, "Mã đặt vé:", safeGetString(bookingData, "booking_id"));
        addInfoRow(layout, "Mã người dùng:", safeGetString(bookingData, "userId"));
        addInfoRow(layout, "Mã suất chiếu:", safeGetString(bookingData, "showtimeId"));

        // Xử lý thời gian an toàn
        Object bookingTimeObj = bookingData.get("bookingTime");
        if (bookingTimeObj instanceof com.google.firebase.Timestamp) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            addInfoRow(layout, "Thời gian đặt:",
                    dateFormat.format(((com.google.firebase.Timestamp) bookingTimeObj).toDate()));
        } else {
            addInfoRow(layout, "Thời gian đặt:", "Không có thông tin");
        }

        // Thêm các thông tin khác với xử lý an toàn
        addInfoRow(layout, "Ghế:", TextUtils.join(", ", safeGetStringList(bookingData, "seats")));
        addInfoRow(layout, "Tổng tiền:", formatCurrency(safeGetInt(bookingData, "totalPrice")));

        // Nút xác nhận
        Button btnConfirm = new Button(this);
        btnConfirm.setText("XÁC NHẬN");
        btnConfirm.setOnClickListener(v -> finish());
        layout.addView(btnConfirm);

        // Hiển thị
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);
        setContentView(scrollView);
    }
    private String safeGetString(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) return "";
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private int safeGetInt(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) return 0;
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    private List<String> safeGetStringList(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) return new ArrayList<>();
        Object value = map.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return new ArrayList<>();
    }
}