package ai.movie.modzy.Activity.Account;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import ai.movie.modzy.MainActivity;
import ai.movie.modzy.R;

public class Login_Activity extends AppCompatActivity {
    EditText emailEditText, passwordEditText;
    Button loginButton;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    CheckBox rememberMeCheckBox;
    ImageView togglePassword;
    boolean isPasswordVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.log_etEmail);
        passwordEditText = findViewById(R.id.log_etPassword);

        rememberMeCheckBox = findViewById(R.id.cbRememberMe);
        togglePassword = findViewById(R.id.ivTogglePassword);
        loginButton = findViewById(R.id.btnLogin);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        loginButton.setOnClickListener(v -> loginUser());
        String fullText = "Chưa có tài khoản? Đăng ký ngay";
        SpannableString spannable = new SpannableString(fullText);

// Vị trí bắt đầu và kết thúc của "Đăng ký ngay"
        int start = fullText.indexOf("Đăng ký ngay");
        int end = start + "Đăng ký ngay".length();

// Đổi màu "Đăng ký ngay"
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.blue_light)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

// Gạch chân "Đăng ký ngay"
        spannable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvGoToRegister.setText(spannable);
        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(Login_Activity.this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(Login_Activity.this, ForgotPasswordActivity.class));
        });
        // Đọc lại email, password nếu đã lưu
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedEmail = prefs.getString("email", "");
        String savedPassword = prefs.getString("password", "");
        boolean isRemembered = prefs.getBoolean("rememberMe", false);
        if (isRemembered) {
            emailEditText.setText(savedEmail);
            passwordEditText.setText(savedPassword);
            rememberMeCheckBox.setChecked(true);
        }

        togglePassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                // Chuyển về ẩn mật khẩu
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_eye_closed_black);
                isPasswordVisible = false;
            } else {
                // Hiển thị mật khẩu
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword.setImageResource(R.drawable.ic_eye_opened);
                isPasswordVisible = true;
            }

            // Giữ con trỏ ở cuối
            passwordEditText.setSelection(passwordEditText.length());
        });


    }
    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 👉 Lưu lại thông tin nếu nhớ tài khoản
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                if (rememberMeCheckBox.isChecked()) {
                    editor.putString("email", email);
                    editor.putString("password", password);
                    editor.putBoolean("rememberMe", true);
                } else {
                    // Xoá thông tin đã lưu nếu người dùng bỏ chọn
                    editor.remove("email");
                    editor.remove("password");
                    editor.putBoolean("rememberMe", false);
                }
                editor.apply();

                String uid = mAuth.getCurrentUser().getUid();
                getUserRoleByUid(uid);
            } else {
                Toast.makeText(this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void getUserRoleByUid(String uid) {
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");

                // Lưu role vào SharedPreferences
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                prefs.edit().putString("user_role", role).apply();

                navigateToHome(role);
            } else {
                Toast.makeText(Login_Activity.this, "Tài khoản không tồn tại trong hệ thống", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(Login_Activity.this, "Lỗi truy cập dữ liệu người dùng", Toast.LENGTH_SHORT).show();
        });
    }



    private void navigateToHome(String role) {
        Intent intent = new Intent(Login_Activity.this, MainActivity.class);
        intent.putExtra("role", role);  // Truyền vai trò người dùng
        startActivity(intent);
        finish();
    }

}
