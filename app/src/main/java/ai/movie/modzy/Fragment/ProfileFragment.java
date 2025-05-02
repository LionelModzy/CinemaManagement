package ai.movie.modzy.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import ai.movie.modzy.Model.User;
import ai.movie.modzy.R;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ProfileFragment extends Fragment {

    private ImageView imgAvatar;
    private EditText etName, etEmail, etRole;
    private Button btnChangeAvatar, btnSaveProfile, btnRemoveAvatar;
    private ProgressBar progressBar;
    private Uri selectedImageUri = null;
    private String avatarUrl = null;
    private String avatarPublicId = null;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imgAvatar.setImageURI(selectedImageUri);
                    uploadNewAvatar();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgAvatar = view.findViewById(R.id.imgAvatar);
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etRole = view.findViewById(R.id.etRole);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnRemoveAvatar = view.findViewById(R.id.btnRemoveAvatar);
        progressBar = view.findViewById(R.id.progressBar);

        btnChangeAvatar.setOnClickListener(v -> openImagePicker());
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnRemoveAvatar.setOnClickListener(v -> showRemoveAvatarConfirmation());

        loadUserProfile();

        return view;
    }

    private void openImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1001);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        }
    }

    private void uploadNewAvatar() {
        String uid = auth.getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);

        // Lưu thông tin ảnh cũ trước khi upload ảnh mới
        db.collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
            User user = snapshot.toObject(User.class);
            String oldPublicId = null;
            if (user != null && user.getAvatarPublicId() != null) {
                oldPublicId = user.getAvatarPublicId();
            }

            // Upload ảnh mới
            // Upload ảnh mới
            MediaManager.get().upload(selectedImageUri)
                    .option("folder", "User")
                    .option("public_id", "user_" + uid + "_" + System.currentTimeMillis()) // Tránh ghi đè
                    .option("resource_type", "image")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            avatarUrl = resultData.get("secure_url").toString();
                            avatarPublicId = resultData.get("public_id").toString();

                            // Cập nhật Firestore
                            updateUserAvatar(uid, avatarUrl, avatarPublicId);
// Sửa lại khai báo oldPublicId thành final

                            final String oldPublicId = (user != null && user.getAvatarPublicId() != null) ? user.getAvatarPublicId() : null;
                            // Chỉ xóa ảnh cũ khi ảnh mới upload thành công
                            if (oldPublicId != null && !oldPublicId.isEmpty()) {
                                deleteImageFromCloudinary(oldPublicId);
                            }
                        }

                        @Override
                        public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Lỗi tải ảnh: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
                    }).dispatch();

        });
    }

    private void updateUserAvatar(String uid, String imageUrl, String publicId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("avatarUrl", imageUrl);
        updates.put("avatarPublicId", publicId);

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Cập nhật ảnh thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showRemoveAvatarConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa ảnh đại diện")
                .setMessage("Bạn có chắc chắn muốn xóa ảnh đại diện?")
                .setPositiveButton("Xóa", (dialog, which) -> removeAvatar())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void removeAvatar() {
        String uid = auth.getCurrentUser().getUid();
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
            User user = snapshot.toObject(User.class);
            if (user != null && user.getAvatarPublicId() != null) {
                deleteImageFromCloudinary(user.getAvatarPublicId());
            }

            // Xóa thông tin ảnh trên Firestore
            Map<String, Object> updates = new HashMap<>();
            updates.put("avatarUrl", "");
            updates.put("avatarPublicId", "");

            db.collection("users").document(uid).update(updates)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        avatarUrl = null;
                        avatarPublicId = null;
                        imgAvatar.setImageResource(R.drawable.ic_person);
                        Toast.makeText(getContext(), "Đã xóa ảnh đại diện", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Lỗi xóa ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void deleteImageFromCloudinary(String publicId) {
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
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

            } catch (Exception e) {
                e.printStackTrace();
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

    private void loadUserProfile() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
            User user = snapshot.toObject(User.class);
            if (user != null) {
                etName.setText(user.getName());
                etEmail.setText(user.getEmail());
                etRole.setText(user.getRole());
                avatarUrl = user.getAvatarUrl();
                avatarPublicId = user.getAvatarPublicId();

                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(requireContext()).load(avatarUrl).into(imgAvatar);
                    btnRemoveAvatar.setVisibility(View.VISIBLE);
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_person);
                    btnRemoveAvatar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void saveProfile() {
        String uid = auth.getCurrentUser().getUid();
        String name = etName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
