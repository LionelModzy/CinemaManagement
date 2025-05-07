package ai.movie.modzy.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ai.movie.modzy.Model.Movies;
import ai.movie.modzy.R;

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

public class MovieSliderAdapter extends RecyclerView.Adapter<MovieSliderAdapter.SliderViewHolder> {

    private final Context context;
    private List<Movies> movieList;

    public MovieSliderAdapter(Context context, List<Movies> movieList) {
        this.context = context;
        this.movieList = movieList;
    }

    public void setMovieList(List<Movies> movies) {
        this.movieList = movies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_slider_movie, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        Movies movie = movieList.get(position);
        holder.sliderTitle.setText(movie.getTitle());
        holder.sliderGenre.setText(movie.getGenre());
        Glide.with(context)
                .load(movie.getPosterUrl())
                .into(holder.sliderPoster);

        // Khi click vào slider item, bắn Intent chuyển sang MovieDetailActivity, truyền movie_id
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

    static class SliderViewHolder extends RecyclerView.ViewHolder {
        TextView sliderTitle, sliderGenre;
        ImageView sliderPoster;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            sliderTitle  = itemView.findViewById(R.id.slider_title);
            sliderGenre  = itemView.findViewById(R.id.slider_genre);
            sliderPoster = itemView.findViewById(R.id.slider_poster);
        }
    }
}
