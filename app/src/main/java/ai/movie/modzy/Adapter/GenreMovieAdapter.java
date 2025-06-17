// GenreMovieAdapter.java
package ai.movie.modzy.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ai.movie.modzy.Activity.Movie.MovieDetailActivity;
import ai.movie.modzy.Model.Movies;
import ai.movie.modzy.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class GenreMovieAdapter extends RecyclerView.Adapter<GenreMovieAdapter.GenreMovieViewHolder> {

    private final Context context;
    private final List<Movies> movieList;
    private final String genre;

    public GenreMovieAdapter(Context context, List<Movies> movieList, String genre) {
        this.context = context;
        this.movieList = movieList;
        this.genre = genre;
    }

    @NonNull
    @Override
    public GenreMovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_movie_genre, parent, false);
        return new GenreMovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreMovieViewHolder holder, int position) {
        Movies movie = movieList.get(position);
        holder.title.setText(movie.getTitle());
        Glide.with(context)
                .load(movie.getPosterUrl())
                .placeholder(R.drawable.bg_image)
                .into(holder.poster);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra("movie_id", movie.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(movieList.size(), 10); // Chỉ hiển thị tối đa 10 phim
    }

    static class GenreMovieViewHolder extends RecyclerView.ViewHolder {
        CircleImageView poster;
        TextView title;

        GenreMovieViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.img_genre_movie_poster);
            title = itemView.findViewById(R.id.tv_genre_movie_title);
        }
    }
}