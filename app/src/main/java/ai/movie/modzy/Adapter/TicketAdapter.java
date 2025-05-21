package ai.movie.modzy.Adapter;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import ai.movie.modzy.Activity.Ticket.TicketDetailActivity;
import ai.movie.modzy.Model.BookingModel;
import ai.movie.modzy.R;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private List<BookingModel> bookingList;

    public TicketAdapter(List<BookingModel> list) {
        this.bookingList = list;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        BookingModel booking = bookingList.get(position);

        // Hiển thị thông tin cơ bản
        holder.tvMovieTitle.setText(booking.getMovieTitle());
        holder.tvCinema.setText("Rạp: " + booking.getCinemaName());
        holder.tvSeats.setText("Ghế: " + TextUtils.join(", ", booking.getSeats()));
        holder.tvTotalPrice.setText("Tổng: " + formatCurrency(booking.getTotalPrice()));

        // Định dạng thời gian
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvBookingTime.setText("Đặt lúc: " + dateFormat.format(booking.getBookingTime().toDate()));

        if (booking.getShowtime() != null) {
            holder.tvShowtime.setText("Suất chiếu: " + dateFormat.format(booking.getShowtime().toDate()));
        }

        // Xử lý click vào item
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), TicketDetailActivity.class);
            intent.putExtra("booking_id", booking.getBookingId());
            v.getContext().startActivity(intent);
        });
    }

    private String formatCurrency(int amount) {
        return String.format("%,d", amount).replace(',', '.') + "đ";
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvMovieTitle, tvCinema, tvSeats, tvTotalPrice, tvBookingTime, tvShowtime;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_ticket);
            tvMovieTitle = itemView.findViewById(R.id.tv_movie_title);
            tvCinema = itemView.findViewById(R.id.tv_cinema);
            tvSeats = itemView.findViewById(R.id.tv_seats);
            tvTotalPrice = itemView.findViewById(R.id.tv_price);
            tvBookingTime = itemView.findViewById(R.id.tv_booking_time);
            tvShowtime = itemView.findViewById(R.id.tv_showtime);
        }
    }
}