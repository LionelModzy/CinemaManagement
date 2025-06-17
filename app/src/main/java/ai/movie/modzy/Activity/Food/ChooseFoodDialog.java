package ai.movie.modzy.Activity.Food;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.movie.modzy.Adapter.ChooseFoodAdapter;
import ai.movie.modzy.Model.Food;
import ai.movie.modzy.R;

public class ChooseFoodDialog extends Dialog {

    private RecyclerView recyclerView;
    private Button btnConfirmFood;
    private ChooseFoodAdapter adapter;
    private FirebaseFirestore db;
    private OnFoodsSelectedListener listener;
    private Map<Food, Integer> selectedFoods = new HashMap<>();

    public interface OnFoodsSelectedListener {
        void onFoodsSelected(Map<Food, Integer> selectedFoods);
    }
    public interface OnFoodTotalChangedListener {
        void onTotalChanged(Map<Food, Integer> selectedFoods);
    }

    private OnFoodTotalChangedListener totalChangedListener;

    public void setOnFoodTotalChangedListener(OnFoodTotalChangedListener listener) {
        this.totalChangedListener = listener;
    }

    public ChooseFoodDialog(@NonNull Context context, OnFoodsSelectedListener listener) {
        super(context);
        this.listener = listener;
    }
    public void setSelectedFoods(Map<Food, Integer> selectedFoods) {
        this.selectedFoods = selectedFoods;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_choose_food);

        recyclerView = findViewById(R.id.recyclerViewFood);
        btnConfirmFood = findViewById(R.id.btnConfirmFood);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChooseFoodAdapter();
        recyclerView.setAdapter(adapter);

        // Thêm listener để cập nhật realtime
        adapter.setOnFoodSelectionChangeListener(selectedFoods -> {
            this.selectedFoods = selectedFoods;
        });

        db = FirebaseFirestore.getInstance();
        loadFoods();

        btnConfirmFood.setOnClickListener(v -> {
            listener.onFoodsSelected(adapter.getSelectedFoodMap());
            dismiss();
        });
    }

    private void loadFoods() {
        db.collection("foods").get().addOnSuccessListener(snapshot -> {
            List<Food> foods = new ArrayList<>();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                Food food = doc.toObject(Food.class);
                // Tìm và cập nhật số lượng từ selectedFoods
                for (Map.Entry<Food, Integer> entry : selectedFoods.entrySet()) {
                    if (entry.getKey().getId().equals(food.getId())) {
                        selectedFoods.put(food, entry.getValue());
                        break;
                    }
                }
                foods.add(food);
            }
            adapter.setFoodList(foods);
            adapter.setSelectedFoodMap(selectedFoods);
        });
    }


}
