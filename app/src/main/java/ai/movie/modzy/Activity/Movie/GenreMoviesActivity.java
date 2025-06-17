package ai.movie.modzy.Activity.Movie;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import ai.movie.modzy.Adapter.MovieAdapter;
import ai.movie.modzy.Adapter.MovieGenreLoadAdapter;
import ai.movie.modzy.Model.Movies;
import ai.movie.modzy.R;

public class GenreMoviesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MovieGenreLoadAdapter adapter;
    private List<Movies> movieList;
    private FirebaseFirestore db;
    private String genre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_movies);

        genre = getIntent().getStringExtra("genre");

        TextView title = findViewById(R.id.tv_genre_title);
        title.setText(getString(R.string.genre_title, genre));

        recyclerView = findViewById(R.id.recycler_genre_movies);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        movieList = new ArrayList<>();
        adapter = new MovieGenreLoadAdapter(this, movieList, false);// false để ẩn control admin
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadMoviesByGenre();
    }

    private void loadMoviesByGenre() {
        db.collection("movies")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    movieList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Movies movie = doc.toObject(Movies.class);
                        if (movie.getGenre() != null && movie.getGenre().contains(genre)) {
                            movieList.add(movie);
                        }
                    }
                    adapter.updateList(movieList);
                });
    }
}