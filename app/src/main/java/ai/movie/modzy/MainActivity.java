package ai.movie.modzy;


import android.content.Intent;
import android.graphics.Movie;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

import ai.movie.modzy.Activity.Movie.AddMovieActivity;
import ai.movie.modzy.Adapter.MovieAdapter;
import ai.movie.modzy.Model.Movies;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movies> movieList;
    private FirebaseFirestore db;
    private Button BtnAddmovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        BtnAddmovie = findViewById(R.id.btnAddMovie);
        movieList = new ArrayList<>();
        adapter = new MovieAdapter(this, movieList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadMovies();

        // Set up the OnClickListener for the Add Movie button
        BtnAddmovie.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMovieActivity.class);
            startActivityForResult(intent, 1); // Chú ý là phải truyền requestCode
        });

        // Set the listener for movie click
        adapter.setOnMovieClickListener(new MovieAdapter.OnMovieClickListener() {
            @Override
            public void onMovieClick(Movies movie) {
                Intent intent = new Intent(MainActivity.this, AddMovieActivity.class);
                intent.putExtra("movie_id", movie.getId());  // Truyền movieId vào AddMovieActivity
                startActivityForResult(intent, 1);
            }
        });
    }

    private void loadMovies() {
        db.collection("movies").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                movieList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Movies movie = document.toObject(Movies.class);
                    movieList.add(movie);
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.e("Firestore", "Error getting movies", task.getException());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadMovies(); // Tải lại danh sách phim sau khi thêm phim mới
        }
    }
}

