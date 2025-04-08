package ai.movie.modzy.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import ai.movie.modzy.Activity.Movie.AddMovieActivity;
import ai.movie.modzy.Adapter.MovieAdapter;
import ai.movie.modzy.Model.Movies;
import ai.movie.modzy.R;

public class MovieFragment extends Fragment {
    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<Movies> movieList;
    private FirebaseFirestore db;
    private Button btnAddMovie;
    private String role;

    // Khai báo launcher để nhận kết quả từ AddMovieActivity
    private ActivityResultLauncher<Intent> addMovieLauncher;

    public MovieFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo launcher
        addMovieLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        loadMovies(); // Tải lại phim sau khi thêm/chỉnh sửa
                    }
                }
        );
    }

    @Nullable
    @Override
    public android.view.View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                          @Nullable Bundle savedInstanceState) {
        android.view.View view = inflater.inflate(R.layout.activity_movie_fragment, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        btnAddMovie = view.findViewById(R.id.btnAddMovie);

        movieList = new ArrayList<>();
        role = getArguments() != null ? getArguments().getString("role", "user") : "user";

        adapter = new MovieAdapter(requireContext(), movieList, role, addMovieLauncher);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        if ("user".equals(role)) {
            btnAddMovie.setVisibility(android.view.View.GONE);
        }

        db = FirebaseFirestore.getInstance();
        loadMovies();

        // Bấm nút "Thêm phim"
        btnAddMovie.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddMovieActivity.class);
            addMovieLauncher.launch(intent);
        });

//        // Khi click vào phim để sửa
//        adapter.setOnMovieClickListener(movie -> {
//            Intent intent = new Intent(getActivity(), AddMovieActivity.class);
//            intent.putExtra("movie_id", movie.getId());
//            addMovieLauncher.launch(intent);
//        });

        return view;
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
            }
        });
    }
}
