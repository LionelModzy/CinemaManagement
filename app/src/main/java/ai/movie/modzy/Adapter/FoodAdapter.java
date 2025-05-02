package ai.movie.modzy.Adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ai.movie.modzy.Activity.Food.AddEditFoodActivity;
import ai.movie.modzy.Model.Food;
import ai.movie.modzy.R;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private Context context;
    private List<Food> foodList;
    private String role;
    private OnFoodClickListener listener;

    public interface OnFoodClickListener {
        void onFoodClick(Food food);
        void onDeleteClick(Food food);
    }

    public void setOnFoodClickListener(OnFoodClickListener listener) {
        this.listener = listener;
    }

    public FoodAdapter(Context context, List<Food> foodList, String role) {
        this.context = context;
        this.foodList = foodList;
        this.role = role;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);
        holder.tvName.setText(food.getName());
        holder.tvPrice.setText(String.format("%.0f VND", food.getPrice()));
        Glide.with(context).load(food.getImageUrl()).into(holder.ivImage);

        // Thiết lập sự kiện click thông thường
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFoodClick(food);
            }
        });

        // Thiết lập sự kiện ấn giữ để hiển thị popup menu
        holder.itemView.setOnLongClickListener(v -> {
            if (role.equals("admin")) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_food_options, popupMenu.getMenu());
                popupMenu.setGravity(Gravity.END);

                popupMenu.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();

                    if (id == R.id.menu_food_edit) {
                        if (listener != null) listener.onFoodClick(food);
                        return true;
                    }

                    if (id == R.id.menu_food_delete) {
                        // Hiển thị dialog xác nhận trước khi xóa
                        new AlertDialog.Builder(context)
                                .setTitle("Xóa món ăn")
                                .setMessage("Bạn có chắc chắn muốn xóa món ăn này?")
                                .setPositiveButton("Xóa", (dialog, which) -> {
                                    // Gọi phương thức xóa từ Activity
                                    if (context instanceof AddEditFoodActivity) {
                                        ((AddEditFoodActivity) context).deleteFood(food);
                                    } else if (listener != null) {
                                        listener.onDeleteClick(food);
                                    }
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                        return true;
                    }

                    return false;
                });

                popupMenu.show();
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageView ivImage;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.foodName);
            tvPrice = itemView.findViewById(R.id.foodPrice);
            ivImage = itemView.findViewById(R.id.foodImage);
        }
    }
}