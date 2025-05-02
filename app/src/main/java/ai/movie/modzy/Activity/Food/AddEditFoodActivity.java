package ai.movie.modzy.Activity.Food;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

import ai.movie.modzy.Model.Food;
import ai.movie.modzy.R;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AddEditFoodActivity extends AppCompatActivity {

    private EditText etName, etPrice;
    private ImageView ivFoodImage;
    private Button btnSave;
    private CheckBox cbIsCombo;
    private LinearLayout layoutComboContainer, layoutComboCheckboxList;
    private String imageUrl;
    private boolean isCombo = false;
    private String foodId = null;
    private FirebaseFirestore db;
    private List<Food> allFoods = new ArrayList<>();
    private List<CheckBox> comboCheckBoxes = new ArrayList<>();
    private String imagePublicId = null;
    private Uri selectedImageUri = null;
    private String oldImagePublicId = null;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_food);

        etName = findViewById(R.id.edtFoodName);
        etPrice = findViewById(R.id.edtFoodPrice);
        ivFoodImage = findViewById(R.id.imgSelectedFood);
        btnSave = findViewById(R.id.btnSaveFood);
        cbIsCombo = findViewById(R.id.checkboxIsCombo);
        layoutComboContainer = findViewById(R.id.layoutComboItems);
        layoutComboCheckboxList = findViewById(R.id.layoutComboCheckboxList);

        db = FirebaseFirestore.getInstance();

        // Check permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1001);
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivFoodImage.setImageURI(selectedImageUri);
                        uploadNewFoodImage();
                    }
                });

        ivFoodImage.setOnClickListener(v -> openImagePicker());
        findViewById(R.id.btnChooseImage).setOnClickListener(v -> openImagePicker());

        cbIsCombo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isCombo = isChecked;
            layoutComboContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (isChecked) loadAvailableFoodsForCombo();
        });

        foodId = getIntent().getStringExtra("food_id");
        if (foodId != null) {
            cbIsCombo.setChecked(false); // reset trước
            loadAvailableFoodsForCombo();
            loadFoodData(foodId);
        }

        btnSave.setOnClickListener(v -> saveFood());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadNewFoodImage() {
        if (selectedImageUri == null) return;

        // Lưu thông tin ảnh cũ trước khi upload ảnh mới
        if (foodId != null) {
            db.collection("foods").document(foodId).get().addOnSuccessListener(documentSnapshot -> {
                Food food = documentSnapshot.toObject(Food.class);
                if (food != null && food.getImagePublicId() != null) {
                    oldImagePublicId = food.getImagePublicId();
                }

                // Upload ảnh mới
                MediaManager.get().upload(selectedImageUri)
                        .option("folder", "Food")
                        .option("public_id", "food_" + (foodId != null ? foodId : UUID.randomUUID().toString()) + "_" + System.currentTimeMillis())
                        .option("resource_type", "image")
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {}

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {}

                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                imageUrl = resultData.get("secure_url").toString();
                                imagePublicId = resultData.get("public_id").toString();

                                // Xóa ảnh cũ nếu có
                                if (oldImagePublicId != null && !oldImagePublicId.isEmpty()) {
                                    deleteImageFromCloudinary(oldImagePublicId);

                                }

                                Toast.makeText(AddEditFoodActivity.this, "Tải ảnh thành công!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                Toast.makeText(AddEditFoodActivity.this, "Lỗi tải ảnh: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {}
                        }).dispatch();
            });
        } else {
            // Trường hợp thêm mới món ăn
            MediaManager.get().upload(selectedImageUri)
                    .option("folder", "Food")
                    .option("public_id", "food_" + UUID.randomUUID().toString() + "_" + System.currentTimeMillis())
                    .option("resource_type", "image")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            imageUrl = resultData.get("secure_url").toString();
                            imagePublicId = resultData.get("public_id").toString();
                            Toast.makeText(AddEditFoodActivity.this, "Tải ảnh thành công!", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Toast.makeText(AddEditFoodActivity.this, "Lỗi tải ảnh: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {}
                    }).dispatch();
        }
    }

//    private void deleteImageFromCloudinary(String publicId, Runnable onSuccess) {
//        Executors.newSingleThreadExecutor().execute(() -> {
//            String cloudName = "dhejopg9q";
//            String apiKey = "193285184664555";
//            String apiSecret = "aqTlPAVHQKyEaJxIHoMxZcyOdDE";
//
//            try {
//                String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
//                String signature = generateSignature(publicId, apiSecret, timestamp);
//
//                String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy";
//
//                OkHttpClient client = new OkHttpClient();
//                RequestBody formBody = new FormBody.Builder()
//                        .add("public_id", publicId)
//                        .add("api_key", apiKey)
//                        .add("timestamp", timestamp)
//                        .add("signature", signature)
//                        .build();
//
//                Request request = new Request.Builder()
//                        .url(url)
//                        .post(formBody)
//                        .build();
//
//                okhttp3.Response response = client.newCall(request).execute();
//                if (response.isSuccessful()) {
//                    // Chạy callback trên UI Thread khi xóa ảnh thành công
//                    runOnUiThread(onSuccess);
//                } else {
//                    Log.e("CloudinaryDelete", "Xóa ảnh không thành công: " + response);
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }


    private void deleteImageFromCloudinary(String publicId) {
        deleteImageFromCloudinary(publicId, new CloudinaryDeleteCallback() {
            @Override
            public void onSuccess() {
                Log.d("Cloudinary", "Xóa ảnh thành công: " + publicId);
            }

            @Override
            public void onError(Exception e) {
                Log.e("Cloudinary", "Lỗi khi xóa ảnh: " + publicId, e);
            }
        });
    }

    private String generateSignature(String publicId, String apiSecret, String timestamp) {
        String toSign = "public_id=" + publicId + "&timestamp=" + timestamp + apiSecret;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(toSign.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private void loadFoodData(String foodId) {
        db.collection("foods").document(foodId).get().addOnSuccessListener(documentSnapshot -> {
            Food food = documentSnapshot.toObject(Food.class);
            if (food == null) return;

            etName.setText(food.getName());
            etPrice.setText(String.valueOf(food.getPrice()));
            imageUrl = food.getImageUrl();
            isCombo = food.isCombo();
            imagePublicId = food.getImagePublicId();
            oldImagePublicId = food.getImagePublicId();

            Glide.with(this).load(imageUrl).into(ivFoodImage);

            if (food.isCombo()) {
                cbIsCombo.setChecked(true);
                layoutComboCheckboxList.postDelayed(() -> checkComboItemsForFood(food), 500);
            }
        });
    }

    private void loadAvailableFoodsForCombo() {
        db.collection("foods").get().addOnSuccessListener(queryDocumentSnapshots -> {
            allFoods.clear();
            layoutComboCheckboxList.removeAllViews();
            comboCheckBoxes.clear();

            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                Food food = doc.toObject(Food.class);
                if (food != null && !food.isCombo()) {
                    allFoods.add(food);
                    CheckBox cb = new CheckBox(this);
                    cb.setText(food.getName());
                    layoutComboCheckboxList.addView(cb);
                    comboCheckBoxes.add(cb);
                }
            }
        });
    }

    private void checkComboItemsForFood(Food food) {
        if (food.getComboItems() == null) return;
        for (int i = 0; i < allFoods.size(); i++) {
            if (food.getComboItems().contains(allFoods.get(i).getId())) {
                comboCheckBoxes.get(i).setChecked(true);
            }
        }
    }

    private void saveFood() {
        String name = etName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || (imageUrl == null && selectedImageUri == null)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isComboChecked = cbIsCombo.isChecked();
        List<String> selectedComboItems = new ArrayList<>();

        if (isComboChecked) {
            for (int i = 0; i < comboCheckBoxes.size(); i++) {
                if (comboCheckBoxes.get(i).isChecked()) {
                    selectedComboItems.add(allFoods.get(i).getId());
                }
            }

            if (selectedComboItems.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 món trong combo", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Food food = new Food(
                foodId == null ? UUID.randomUUID().toString() : foodId,
                name,
                price,
                imageUrl,
                isComboChecked,
                imagePublicId,
                selectedComboItems
        );

        db.collection("foods").document(food.getId()).set(food)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

//    // Thêm phương thức này để xử lý xóa món ăn
//    public void deleteFood(Food food) {
//        new AlertDialog.Builder(this)
//                .setTitle("Xóa món ăn")
//                .setMessage("Bạn có chắc chắn muốn xóa món ăn này?")
//                .setPositiveButton("Xóa", (dialog, which) -> {
//                    if (food.getImagePublicId() != null && !food.getImagePublicId().isEmpty()) {
//                        deleteImageFromCloudinary(food.getImagePublicId(), () -> {
//                            // Sau khi xóa ảnh xong thì xóa Firestore
//                            db.collection("foods").document(food.getId()).delete()
//                                    .addOnSuccessListener(aVoid -> {
//                                        Toast.makeText(this, "Đã xóa món ăn", Toast.LENGTH_SHORT).show();
//                                        setResult(RESULT_OK);
//                                        finish();
//                                    })
//                                    .addOnFailureListener(e -> {
//                                        Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                    });
//                        });
//                    } else {
//                        // Nếu không có ảnh thì chỉ xóa Firestore
//                        db.collection("foods").document(food.getId()).delete()
//                                .addOnSuccessListener(aVoid -> {
//                                    Toast.makeText(this, "Đã xóa món ăn", Toast.LENGTH_SHORT).show();
//                                    setResult(RESULT_OK);
//                                    finish();
//                                })
//                                .addOnFailureListener(e -> {
//                                    Toast.makeText(this, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                                });
//                    }
//                })
//                .setNegativeButton("Hủy", null)
//                .show();
//    }

    // Sửa lại phương thức deleteFood để đảm bảo xóa cả ảnh
    public void deleteFood(Food food) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa món ăn")
                .setMessage("Bạn có chắc chắn muốn xóa món ăn này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Tạo một biến final để lưu publicId
                    final String publicIdToDelete = food.getImagePublicId();

                    // Xóa document từ Firestore trước
                    db.collection("foods").document(food.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                // Sau khi xóa Firestore thành công, xóa ảnh từ Cloudinary nếu có
                                if (publicIdToDelete != null && !publicIdToDelete.isEmpty()) {
                                    deleteImageFromCloudinary(publicIdToDelete, new CloudinaryDeleteCallback() {
                                        @Override
                                        public void onSuccess() {
                                            runOnUiThread(() -> {
                                                Toast.makeText(AddEditFoodActivity.this, "Đã xóa món ăn và ảnh", Toast.LENGTH_SHORT).show();
                                                setResult(RESULT_OK);
                                                finish();
                                            });
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            runOnUiThread(() -> {
                                                Toast.makeText(AddEditFoodActivity.this, "Đã xóa món ăn nhưng có lỗi khi xóa ảnh", Toast.LENGTH_SHORT).show();
                                                setResult(RESULT_OK);
                                                finish();
                                            });
                                        }
                                    });
                                } else {
                                    // Nếu không có ảnh để xóa
                                    Toast.makeText(AddEditFoodActivity.this, "Đã xóa món ăn", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddEditFoodActivity.this, "Lỗi khi xóa món ăn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Thêm interface callback cho xóa ảnh
    interface CloudinaryDeleteCallback {
        void onSuccess();
        void onError(Exception e);
    }

    // Cập nhật phương thức deleteImageFromCloudinary để sử dụng callback
    private void deleteImageFromCloudinary(String publicId, CloudinaryDeleteCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            String cloudName = "dhejopg9q";
            String apiKey = "193285184664555";
            String apiSecret = "aqTlPAVHQKyEaJxIHoMxZcyOdDE";

            try {
                String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
                String signature = generateSignature(publicId, apiSecret, timestamp);

                String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy";

                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormBody.Builder()
                        .add("public_id", publicId)
                        .add("api_key", apiKey)
                        .add("timestamp", timestamp)
                        .add("signature", signature)
                        .build();

                Request request = new Request.Builder()
                        .url(url)
                        .post(formBody)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(new IOException("Unexpected code " + response));
                }

            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    // ... (phần code sau giữ nguyên)
}

