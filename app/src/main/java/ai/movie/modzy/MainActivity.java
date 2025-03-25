package ai.movie.modzy;

import android.graphics.Movie;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

import ai.movie.modzy.Adapter.MovieAdapter;
import ai.movie.modzy.Model.Movies;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movies> movieList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        movieList = new ArrayList<>();
        adapter = new MovieAdapter(this, movieList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadMovies();
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
}
