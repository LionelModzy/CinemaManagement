package ai.movie.modzy.Activity.Account;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import ai.movie.modzy.MainActivity;
import ai.movie.modzy.R;

public class Login_Activity extends AppCompatActivity {
    EditText emailEditText, passwordEditText;
    Button loginButton;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.log_etEmail);
        passwordEditText = findViewById(R.id.log_etPassword);
        loginButton = findViewById(R.id.btnLogin);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        loginButton.setOnClickListener(v -> loginUser());
        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(Login_Activity.this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(Login_Activity.this, ForgotPasswordActivity.class));
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
                String uid = mAuth.getCurrentUser().getUid();
                getUserRoleByUid(uid); // dùng uid luôn, tối ưu hơn
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
