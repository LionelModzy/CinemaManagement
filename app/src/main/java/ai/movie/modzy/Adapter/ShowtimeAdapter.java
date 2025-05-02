package ai.movie.modzy.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;

import ai.movie.modzy.Activity.Booking.AddShowtimeActivity;
import ai.movie.modzy.Activity.Booking.BookingMovieActivity;
import ai.movie.modzy.Model.Showtime;
import ai.movie.modzy.R;

public class ShowtimeAdapter extends RecyclerView.Adapter<ShowtimeAdapter.ShowtimeViewHolder> {

    private Context context;
    private List<Showtime> showtimeList;
    private String role;
    private int selectedPosition = -1; // ban đầu chưa chọn item nào

    public ShowtimeAdapter(Context context, List<Showtime> showtimeList,  String role) {
        this.context = context;
        this.showtimeList = showtimeList;
        this.role = role;
    }

    @NonNull
    @Override
    public ShowtimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_showtime, parent, false);
        return new ShowtimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowtimeViewHolder holder, int position) {
        Showtime showtime = showtimeList.get(position);
        holder.tvCinema.setText("Rạp: " + showtime.getCinema());
        holder.tvRoom.setText("Phòng: " + showtime.getRoom());

        Timestamp timestamp = showtime.getDatetime();
        if (timestamp != null) {
            String formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(timestamp.toDate());
            holder.tvTime.setText("Thời gian: " + formattedDate);
        } else {
            holder.tvTime.setText("Thời gian: Không rõ");
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (role.equals("admin")) {
                PopupMenu popupMenu = new PopupMenu(context, v);
                popupMenu.getMenuInflater().inflate(R.menu.menu_showtime_options, popupMenu.getMenu());

                // Thêm dòng này để popup hiện bên phải
                popupMenu.setGravity(Gravity.END);

                popupMenu.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();

                    if (id == R.id.menu_edit) {
                        Intent i = new Intent(context, AddShowtimeActivity.class);
                        i.putExtra("showtime_id", showtime.getId());
                        context.startActivity(i);
                        return true;
                    }

                    if (id == R.id.menu_delete) {
                        FirebaseFirestore.getInstance()
                                .collection("showtimes")
                                .document(showtime.getId())
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(context, "Đã xóa suất chiếu", Toast.LENGTH_SHORT).show();
                                    int currentPosition = holder.getAdapterPosition();
                                    if (currentPosition != RecyclerView.NO_POSITION) {
                                        showtimeList.remove(currentPosition);
                                        notifyItemRemoved(currentPosition);
                                    }
                                });
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
        return showtimeList.size();
    }

    public static class ShowtimeViewHolder extends RecyclerView.ViewHolder {
        TextView tvCinema, tvRoom, tvTime;
        Button btnEdit, btnDelete;
        public ShowtimeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCinema = itemView.findViewById(R.id.tv_cinema);
            tvRoom = itemView.findViewById(R.id.tv_room);
            tvTime = itemView.findViewById(R.id.tv_time);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
