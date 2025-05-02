package ai.movie.modzy.Activity.Food;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import ai.movie.modzy.Adapter.FoodAdapter;
import ai.movie.modzy.Model.Food;
import ai.movie.modzy.R;

public class FoodListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private List<Food> foodList = new ArrayList<>();
    private FoodAdapter adapter;
    private FirebaseFirestore db;
    private String role = "user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        recyclerView = findViewById(R.id.recyclerViewFood);
        fabAdd = findViewById(R.id.fabAddFood);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        role = getIntent().getStringExtra("role");
        if (!"admin".equals(role)) {
            fabAdd.setVisibility(View.GONE);
        }

        adapter = new FoodAdapter(this, foodList, role);
        recyclerView.setAdapter(adapter);

        adapter.setOnFoodClickListener(new FoodAdapter.OnFoodClickListener() {
            @Override
            public void onFoodClick(Food food) {
                Intent intent = new Intent(FoodListActivity.this, AddEditFoodActivity.class);
                intent.putExtra("food_id", food.getId());
                startActivityForResult(intent, 100);
            }

            @Override
            public void onDeleteClick(Food food) {
                db.collection("foods").document(food.getId()).delete().addOnSuccessListener(aVoid -> {
                    Toast.makeText(FoodListActivity.this, "Xóa thành công", Toast.LENGTH_SHORT).show();
                    loadFoods();
                });
            }
        });

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(FoodListActivity.this, AddEditFoodActivity.class);
            startActivityForResult(intent, 100);
        });

        loadFoods();
    }

    private void loadFoods() {
        db.collection("foods").get().addOnSuccessListener(querySnapshot -> {
            foodList.clear();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                foodList.add(doc.toObject(Food.class));
            }
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            loadFoods();
        }
    }
}
