package ai.movie.modzy.Activity.Account;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import ai.movie.modzy.MainActivity;
import ai.movie.modzy.R;

class Login_Activity extends AppCompatActivity {
    private EditText edtIdLogin;
    private Button btnLogin;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtIdLogin = findViewById(R.id.login);
        btnLogin = findViewById(R.id.btn_login);
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(view -> {
            String idlogin = edtIdLogin.getText().toString().trim();
            if (!idlogin.isEmpty()) {
                loginWithIdLogin(idlogin);
            } else {
                Toast.makeText(this, "Vui lòng nhập ID Login", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginWithIdLogin(String idlogin) {
        // Tìm user theo idlogin
        db.collection("users")
                .whereEqualTo("idlogin", idlogin)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String role = document.getString("role");
                        navigateToHome(role);
                    } else {
                        Toast.makeText(Login_Activity.this, "ID Login không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToHome(String role) {
        if ("manager".equals(role)) {
            startActivity(new Intent(Login_Activity.this, MainActivity.class));
        }
//        } else if ("employee".equals(role)) {
//            startActivity(new Intent(LoginActivity.this, EmployeeActivity.class));
//        } else {
//            startActivity(new Intent(LoginActivity.this, .class));
//        }
        finish();
    }
}
