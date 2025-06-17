package ai.movie.modzy.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.movie.modzy.Adapter.UserAdapter;
import ai.movie.modzy.Model.User;
import ai.movie.modzy.R;

public class ManageAccountFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_account, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewAccounts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        FloatingActionButton btnAdd = view.findViewById(R.id.btnAddAccount);

        btnAdd.setOnClickListener(v -> showAddAccountDialog());
        adapter = new UserAdapter(userList, getContext(), new UserAdapter.OnUserActionListener() {
            @Override
            public void onEdit(User user) {
                showEditDialog(user);
            }

            @Override
            public void onDelete(User user) {
                showDeleteConfirmation(user);
            }
        });
        recyclerView.setAdapter(adapter);

        loadUsers();
        return view;
    }

    private void loadUsers() {
        db.collection("users").get().addOnSuccessListener(querySnapshot -> {
            userList.clear();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                User user = doc.toObject(User.class);
                user.setId(doc.getId());
                userList.add(user);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void showEditDialog(User user) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_user, null);
        EditText etName = dialogView.findViewById(R.id.etEditName);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerEditRole);

        etName.setText(user.getName());
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, new String[]{"user", "admin"});
        spinnerRole.setAdapter(roleAdapter);
        spinnerRole.setSelection(user.getRole().equals("admin") ? 1 : 0);

        new AlertDialog.Builder(getContext())
                .setTitle("Sửa thông tin")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newRole = spinnerRole.getSelectedItem().toString();

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("name", newName);
                    updates.put("role", newRole);

                    db.collection("users").document(user.getId()).update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Đã cập nhật", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteConfirmation(User user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc muốn xóa tài khoản này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("users").document(user.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Đã xóa", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void showAddAccountDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_user, null);
        EditText etName = dialogView.findViewById(R.id.etNewName);
        EditText etEmail = dialogView.findViewById(R.id.etNewEmail);
        EditText etPassword = dialogView.findViewById(R.id.etNewPassword);
        Spinner spinnerRole = dialogView.findViewById(R.id.spinnerNewRole);

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_dropdown_item, new String[]{"user", "admin"});
        spinnerRole.setAdapter(roleAdapter);

        new AlertDialog.Builder(getContext())
                .setTitle("Thêm tài khoản mới")
                .setView(dialogView)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();
                    String password = etPassword.getText().toString().trim();
                    String role = spinnerRole.getSelectedItem().toString();

                    if (name.isEmpty() || email.isEmpty() || password.length() < 6) {
                        Toast.makeText(getContext(), "Thông tin không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createNewAccount(name, email, password, role);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void createNewAccount(String name, String email, String password, String role) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("name", name);
                    userData.put("email", email);
                    userData.put("role", role);
                    userData.put("avatarUrl", ""); // hoặc default avatar

                    db.collection("users").document(uid).set(userData)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}

