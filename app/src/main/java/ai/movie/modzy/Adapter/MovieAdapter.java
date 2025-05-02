package ai.movie.modzy.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Movie;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import ai.movie.modzy.Activity.Movie.AddMovieActivity;
import ai.movie.modzy.Activity.Movie.MovieDetailActivity;
import ai.movie.modzy.MainActivity;
import ai.movie.modzy.Model.Movies; // Import đúng model Movies
import ai.movie.modzy.R; // Import đúng R

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private Context context;
    private List<Movies> moviesList;
    private OnMovieClickListener onMovieClickListener;
    private String role;
    private ActivityResultLauncher<Intent> editMovieLauncher;
    public MovieAdapter(Context context, List<Movies> moviesList, String role,  ActivityResultLauncher<Intent> launcher) {
        this.context = context;
        this.moviesList = moviesList;
        this.role = role;
        this.editMovieLauncher = launcher;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movies movie = moviesList.get(position);
        holder.title.setText(movie.getTitle());
        holder.genre.setText(movie.getGenre());
        holder.duration.setText(movie.getDuration() + " min");
        holder.rating.setText("Rating: " + movie.getRating());

        Glide.with(context).load(movie.getPosterUrl()).into(holder.poster);

        // Sự kiện click để mở màn hình chi tiết
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra("movie_id", movie.getId());
            context.startActivity(intent);
        });
        int visibility = "user".equals(role) ? View.GONE : View.VISIBLE;
        holder.editButton.setVisibility(visibility);
        holder.deleteButton.setVisibility(visibility);
//if ("user".equals(role)) {
//    holder.editButton.setVisibility(View.GONE);
//    holder.deleteButton.setVisibility(View.GONE);
//} else {
//    holder.editButton.setVisibility(View.VISIBLE);
//    holder.deleteButton.setVisibility(View.VISIBLE);
//}

        // Nhấn vào "Sửa" để mở AddMovieActivity với movie_id
        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddMovieActivity.class);
            intent.putExtra("movie_id", String.valueOf(movie.getId()));
            editMovieLauncher.launch(intent); // dùng launcher thay vì startActivityForResult
        });


        // Nhấn vào "Xóa" để xóa khỏi Firestore
//        holder.deleteButton.setOnClickListener(v -> deleteMovie(movie.getId(), position));  // Corrected line
//    }
        holder.deleteButton.setOnClickListener(v -> deleteMovie(movie.getId(),position));
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView title, genre, rating, duration;
        ImageView poster;
        Button  editButton, deleteButton;
        public MovieViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.movie_title);
            genre = itemView.findViewById(R.id.movie_genre);
            rating = itemView.findViewById(R.id.movie_rating);
            poster = itemView.findViewById(R.id.movie_poster);
            duration = itemView.findViewById(R.id.movie_duration);
            editButton = itemView.findViewById(R.id.btn_edit_movie);
            deleteButton = itemView.findViewById(R.id.btn_delete_movie);
        }
    }


    // Phương thức deleteMovie() cần sửa lại để nhận đúng kiểu dữ liệu
    // Phương thức deleteMovie() cần sửa lại để nhận đúng kiểu dữ liệu
    private void deleteMovie(int movieId, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("movies").document(String.valueOf(movieId))  // Convert int to String
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (position >= 0 && position < moviesList.size()) {
                        moviesList.remove(position);
                        notifyItemRemoved(position);
                    }
                    Toast.makeText(context, "Đã xóa phim", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi xóa phim", Toast.LENGTH_SHORT).show());
    }


    public interface OnMovieClickListener {
        void onMovieClick(Movies movie);
    }

    // Provide a setter method to set the listener from outside the adapter
    public void setOnMovieClickListener(OnMovieClickListener listener) {
        this.onMovieClickListener = listener;
    }


}



