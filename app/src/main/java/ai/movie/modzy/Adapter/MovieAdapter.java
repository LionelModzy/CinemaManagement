package ai.movie.modzy.Adapter;

import android.content.Context;
import android.graphics.Movie;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ai.movie.modzy.Model.Movies; // Import đúng model Movies
import ai.movie.modzy.R; // Import đúng R

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    private Context context;
    private List<Movies> moviesList; // Đổi từ Movie -> Movies

    public MovieAdapter(Context context, List<Movies> moviesList) { // Đổi từ Movie -> Movies
        this.context = context;
        this.moviesList = moviesList;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movies movies = moviesList.get(position); // Đổi từ Movie -> Movies
        holder.title.setText(movies.getTitle());
        holder.genre.setText(movies.getGenre());
        holder.rating.setText("Rating: " + movies.getRating());

        // Dùng Glide để load ảnh từ URL
        Glide.with(context).load(movies.getPosterUrl()).into(holder.poster);
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView title, genre, rating;
        ImageView poster;

        public MovieViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.movie_title);
            genre = itemView.findViewById(R.id.movie_genre);
            rating = itemView.findViewById(R.id.movie_rating);
            poster = itemView.findViewById(R.id.movie_poster);
        }
    }
}
