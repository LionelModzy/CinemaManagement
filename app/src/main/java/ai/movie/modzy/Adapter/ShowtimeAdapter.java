package ai.movie.modzy.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookingMovieActivity.class);
            intent.putExtra("showtime_id", showtime.getId()); // truyền id suất chiếu
            context.startActivity(intent);
        });
        if (role.equals("admin")) {
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);

            // Sửa
            holder.btnEdit.setOnClickListener(v -> {
                Intent i = new Intent(context, AddShowtimeActivity.class);
                i.putExtra("showtime_id", showtime.getId());
                context.startActivity(i);
            });

            // Xóa
            holder.btnDelete.setOnClickListener(v -> {
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

                            notifyItemRemoved(position);
                        });
            });
        }
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
