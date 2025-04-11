package ai.movie.modzy.Activity.Account;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ai.movie.modzy.R;

public class RegisterActivity extends AppCompatActivity {
    EditText etEmail, etPassword, etName;
    Button btnRegister;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    ImageView ivTogglePassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.reg_etEmail);
        etPassword = findViewById(R.id.reg_etPassword);
        etName = findViewById(R.id.reg_etName);
        btnRegister = findViewById(R.id.btnRegister);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        ImageView ivTogglePassword = findViewById(R.id.ivTogglePassword);
        EditText etPassword = findViewById(R.id.reg_etPassword);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);
        String fullText = "Đã có tài khoản? Đăng nhập ngay";
        SpannableString spannable = new SpannableString(fullText);

// Vị trí bắt đầu và kết thúc của "Đăng ký ngay"
        int start = fullText.indexOf("Đăng nhập ngay");
        int end = start + "Đăng nhập ngay".length();
        // Đổi màu "Đăng ký ngay"
        spannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.blue_light)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

// Gạch chân "Đăng ký ngay"
        spannable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvGoToLogin.setText(spannable);
        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, Login_Activity.class));
        });


        ivTogglePassword.setOnClickListener(v -> {
            if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_opened);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_closed_black);
            }
            etPassword.setSelection(etPassword.getText().length()); // đưa con trỏ về cuối
        });
        btnRegister.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String name = etName.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = task.getResult().getUser().getUid();
                    Map<String, Object> user = new HashMap<>();
                    user.put("email", email);
                    user.put("name", name);
                    user.put("role", "user"); // mặc định role là user
//                    user.put("idlogin", uid);

                    db.collection("users").document(uid).set(user).addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, Login_Activity.class));
                        finish();
                    });
                } else {
                    Toast.makeText(this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}


