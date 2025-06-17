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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import ai.movie.modzy.Activity.Movie.MovieDetailActivity;
import ai.movie.modzy.Model.Movies;
import ai.movie.modzy.R;

public class MovieGenreLoadAdapter extends RecyclerView.Adapter<MovieGenreLoadAdapter.MovieViewHolder> {
    private Context context;
    private List<Movies> moviesList;
    private boolean showAdminControls;

    public MovieGenreLoadAdapter(Context context, List<Movies> moviesList, boolean showAdminControls) {
        this.context = context;
        this.moviesList = moviesList;
        this.showAdminControls = showAdminControls;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_movie_grid, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movies movie = moviesList.get(position);

        // Load poster với bo góc
        Glide.with(context)
                .load(movie.getPosterUrl())
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(16)))
                .placeholder(R.drawable.ic_person)
                .into(holder.poster);

        holder.title.setText(movie.getTitle());

        // Format rating
        holder.rating.setText(String.format("%.1f", movie.getRating()));

        // Xử lý click vào item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra("movie_id", movie.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }

    public void updateList(List<Movies> newList) {
        moviesList = newList;
        notifyDataSetChanged();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, rating;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.img_movie_poster);
            title = itemView.findViewById(R.id.tv_movie_title);
            rating = itemView.findViewById(R.id.tv_movie_rating);
        }
    }
}
