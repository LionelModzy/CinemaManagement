package ai.movie.modzy.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import ai.movie.modzy.Adapter.TicketAdapter;
import ai.movie.modzy.Model.BookingModel;
import ai.movie.modzy.R;

public class TicketFragment extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView recyclerView;
    private TicketAdapter adapter;
    private List<BookingModel> ticketList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ticket, container, false);

        recyclerView = view.findViewById(R.id.recycler_tickets);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TicketAdapter(ticketList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadUserTickets();

        return view;
    }

    private void loadUserTickets() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("bookings")
                .whereEqualTo("userId", userId)
                .orderBy("bookingTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ticketList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        BookingModel booking = doc.toObject(BookingModel.class);
                        if (booking != null) {
                            booking.setBookingId(doc.getId());
                            loadAdditionalBookingInfo(booking);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi tải vé: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadAdditionalBookingInfo(BookingModel booking) {
        // Load thông tin suất chiếu
        db.collection("showtimes").document(booking.getShowtimeId())
                .get()
                .addOnSuccessListener(showtimeDoc -> {
                    if (showtimeDoc.exists()) {
                        booking.setCinemaName(showtimeDoc.getString("cinema"));
                        booking.setShowtime(showtimeDoc.getTimestamp("datetime"));

                        // Load thông tin phim
                        Long movieId = showtimeDoc.getLong("movieID");
                        if (movieId != null) {
                            db.collection("movies").document(String.valueOf(movieId))
                                    .get()
                                    .addOnSuccessListener(movieDoc -> {
                                        if (movieDoc.exists()) {
                                            booking.setMovieTitle(movieDoc.getString("title"));
                                            ticketList.add(booking);
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    }
                });
    }
}