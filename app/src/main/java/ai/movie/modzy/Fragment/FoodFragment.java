package ai.movie.modzy.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import ai.movie.modzy.Activity.Food.AddEditFoodActivity;
import ai.movie.modzy.Adapter.FoodAdapter;
import ai.movie.modzy.Model.Food;
import ai.movie.modzy.R;

public class FoodFragment extends Fragment {

    private RecyclerView recyclerView;
    private FoodAdapter adapter;
    private List<Food> foodList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FloatingActionButton fabAddFood;
    private String role = "user"; // Mặc định

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food, container, false);

        recyclerView = view.findViewById(R.id.recyclerFoods);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Bundle bundle = getArguments();
        if (bundle != null) {
            role = bundle.getString("role", "user");
        }

        adapter = new FoodAdapter(getContext(), foodList, role);
        recyclerView.setAdapter(adapter);


        adapter.setOnFoodClickListener(new FoodAdapter.OnFoodClickListener() {
            @Override
            public void onFoodClick(Food food) {
                Intent intent = new Intent(getContext(), AddEditFoodActivity.class);
                intent.putExtra("food_id", food.getId());
                startActivityForResult(intent, 123); // sửa

            }

            @Override
            public void onDeleteClick(Food food) {
                db.collection("foods").document(food.getId()).delete().addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Xóa thành công", Toast.LENGTH_SHORT).show();
                    loadFoods();
                });
            }
        });

        fabAddFood = view.findViewById(R.id.fabAddFood);

        if (bundle != null) {
            role = bundle.getString("role", "user");
        }

        if (!role.equals("admin")) {
            fabAddFood.setVisibility(View.GONE);
        }

        fabAddFood.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddEditFoodActivity.class));
        });

        loadFoods();
        return view;
    }

    private void loadFoods() {
        db.collection("foods").get().addOnSuccessListener(queryDocumentSnapshots -> {
            foodList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Food food = doc.toObject(Food.class);
                foodList.add(food);
            }
            adapter.notifyDataSetChanged();
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        loadFoods();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == getActivity().RESULT_OK) {
            loadFoods(); // Reload lại danh sách nếu sửa thành công
        }
    }

}
