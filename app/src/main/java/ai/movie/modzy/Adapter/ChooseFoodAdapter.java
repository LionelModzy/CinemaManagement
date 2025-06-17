package ai.movie.modzy.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.movie.modzy.Model.Food;
import ai.movie.modzy.R;

public class ChooseFoodAdapter extends RecyclerView.Adapter<ChooseFoodAdapter.ViewHolder> {
    private List<Food> foodList = new ArrayList<>();
    private Map<Food, Integer> selectedFoodMap = new HashMap<>();
    // Thêm interface để bắn sự kiện khi có thay đổi
    public interface OnFoodSelectionChange {
        void onSelectionChanged(Map<Food, Integer> selectedFoods);
    }
    private OnFoodSelectionChange listener;

    public void setOnFoodSelectionChangeListener(OnFoodSelectionChange listener) {
        this.listener = listener;
    }
    public Map<Food, Integer> getSelectedFoodMap() {
        return selectedFoodMap;
    }

    public void setFoodList(List<Food> foodList) {
        this.foodList = foodList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFood;
        TextView tvName, tvPrice, tvQuantity;
        ImageButton btnPlus, btnMinus;

        public ViewHolder(View view) {
            super(view);
            imgFood = view.findViewById(R.id.imgFood);
            tvName = view.findViewById(R.id.tvName);
            tvPrice = view.findViewById(R.id.tvPrice);
            tvQuantity = view.findViewById(R.id.tvQuantity);
            btnPlus = view.findViewById(R.id.btnPlus);
            btnMinus = view.findViewById(R.id.btnMinus);
        }
    }

    @Override
    public ChooseFoodAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_choose_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Food food = foodList.get(position);
        holder.tvName.setText(food.getName());
        holder.tvPrice.setText(String.format("%,dđ", (int)food.getPrice()));

        Glide.with(holder.imgFood.getContext()).load(food.getImageUrl()).into(holder.imgFood);

        int quantity = selectedFoodMap.getOrDefault(food, 0);
        holder.tvQuantity.setText(String.valueOf(quantity));

        // Cập nhật trạng thái dựa trên số lượng
        boolean isSelected = quantity > 0;
        holder.itemView.setBackgroundColor(isSelected ? Color.LTGRAY : Color.WHITE);
        holder.btnPlus.setEnabled(isSelected); // Chỉ cho tăng nếu đã chọn
        // Luôn enabled
        holder.btnMinus.setEnabled(isSelected);

        holder.itemView.setOnClickListener(v -> {
            if (quantity == 0) {
                selectedFoodMap.put(food, 1);
                holder.tvQuantity.setText("1");
                holder.itemView.setBackgroundColor(Color.LTGRAY);
                holder.btnMinus.setEnabled(true);
            } else {
                selectedFoodMap.remove(food);
                holder.tvQuantity.setText("0");
                holder.itemView.setBackgroundColor(Color.WHITE);
                holder.btnMinus.setEnabled(false);
            }
            if (listener != null) {
                listener.onSelectionChanged(selectedFoodMap);
            }
            notifyItemChanged(position);
        });

        holder.btnPlus.setOnClickListener(v -> {
            int newQuantity = quantity + 1;
            selectedFoodMap.put(food, newQuantity);
            holder.tvQuantity.setText(String.valueOf(newQuantity));
            if (listener != null) {
                listener.onSelectionChanged(selectedFoodMap);
            }
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                int newQuantity = quantity - 1;
                selectedFoodMap.put(food, newQuantity);
                holder.tvQuantity.setText(String.valueOf(newQuantity));
            } else {
                selectedFoodMap.remove(food);
                holder.tvQuantity.setText("0");
                holder.itemView.setBackgroundColor(Color.WHITE);
                holder.btnMinus.setEnabled(false);
            }
            if (listener != null) {
                listener.onSelectionChanged(selectedFoodMap);
            }
            notifyItemChanged(position);
        });
    }

    public void setSelectedFoodMap(Map<Food, Integer> selectedFoodMap) {
        this.selectedFoodMap = selectedFoodMap;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }
}

