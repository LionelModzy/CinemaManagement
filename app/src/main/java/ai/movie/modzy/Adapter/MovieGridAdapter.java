package ai.movie.modzy.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ai.movie.modzy.Activity.Movie.MovieDetailActivity;
import ai.movie.modzy.Model.Movies;
import ai.movie.modzy.R;

public class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.MovieViewHolder> {

    private final Context context;
    private List<Movies> movieList;

    public MovieGridAdapter(Context context, List<Movies> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    public void setMovieList(List<Movies> movieList) {
        this.movieList = movieList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_movie_grid, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movies movie = movieList.get(position);
        holder.title.setText(movie.getTitle());
        Glide.with(context)
                .load(movie.getPosterUrl())
                .placeholder(R.drawable.bg_image)
                .into(holder.poster);

        // Click vào grid item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra("movie_id", movie.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movieList != null ? movieList.size() : 0;
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.img_movie_poster);
            title  = itemView.findViewById(R.id.tv_movie_title);
        }
    }
}
