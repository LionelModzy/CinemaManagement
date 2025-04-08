package ai.movie.modzy.Activity.Movie;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ai.movie.modzy.R;

public class AddMovieActivity extends AppCompatActivity {
    private EditText etTitle, etGenre, etDuration, etPosterUrl, etDescription, etRating, etReleaseDate, etTrailerUrl, etAuthors, etActors, etCountry, etTmdbId;
    private Button btnSave;
    private FirebaseFirestore db;
    private String movieId = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_movie);
        db = FirebaseFirestore.getInstance();

        // Ánh xạ các trường nhập liệu
        etTitle = findViewById(R.id.etTitle);
        etGenre = findViewById(R.id.etGenre);
        etDuration = findViewById(R.id.etDuration);
        etPosterUrl = findViewById(R.id.etPosterUrl);
        etDescription = findViewById(R.id.etDescription);
        etRating = findViewById(R.id.etRating);
        etReleaseDate = findViewById(R.id.etReleaseDate);
        etTrailerUrl = findViewById(R.id.etTrailerUrl);
        etAuthors = findViewById(R.id.etAuthors);
        etActors = findViewById(R.id.etActors);
        etCountry = findViewById(R.id.etCountry);
        etTmdbId = findViewById(R.id.etTmdbId);
        btnSave = findViewById(R.id.btnSave);

        // Kiểm tra nếu có movieId (chỉnh sửa phim)
        movieId = getIntent().getStringExtra("movie_id");
        if (movieId != null) {
            loadMovieData(movieId);
        }

        btnSave.setOnClickListener(v -> saveMovie());

    }
    private void loadMovieData(String movieId) {
        db.collection("movies").document(movieId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                etTitle.setText(documentSnapshot.getString("title"));
                etGenre.setText(documentSnapshot.getString("genre"));
                etDuration.setText(String.valueOf(documentSnapshot.getLong("duration")));
                etPosterUrl.setText(documentSnapshot.getString("posterUrl"));
                etDescription.setText(documentSnapshot.getString("description"));
                etRating.setText(String.valueOf(documentSnapshot.getDouble("rating")));
                etReleaseDate.setText(documentSnapshot.getString("releaseDate"));
                etTrailerUrl.setText(documentSnapshot.getString("trailerUrl"));
                etCountry.setText(documentSnapshot.getString("country"));
                etTmdbId.setText(String.valueOf(documentSnapshot.getLong("tmdbId")));

                List<String> authors = (List<String>) documentSnapshot.get("authors");
                etAuthors.setText(authors != null ? String.join(", ", authors) : "");

                List<String> actors = (List<String>) documentSnapshot.get("actors");
                etActors.setText(actors != null ? String.join(", ", actors) : "");
            }
        });
    }

    private void saveMovie() {
        String title = etTitle.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();
        String durationStr = etDuration.getText().toString().trim();
        String posterUrl = etPosterUrl.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String ratingStr = etRating.getText().toString().trim();
        String releaseDate = etReleaseDate.getText().toString().trim();
        String trailerUrl = etTrailerUrl.getText().toString().trim();
        String authorsStr = etAuthors.getText().toString().trim();
        String actorsStr = etActors.getText().toString().trim();
        String country = etCountry.getText().toString().trim();
        String tmdbIdStr = etTmdbId.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(genre) || TextUtils.isEmpty(durationStr) || TextUtils.isEmpty(ratingStr)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        int duration;
        double rating;
        int tmdbId = 0;

        try {
            duration = Integer.parseInt(durationStr);
            rating = Double.parseDouble(ratingStr);
            if (!TextUtils.isEmpty(tmdbIdStr)) {
                tmdbId = Integer.parseInt(tmdbIdStr);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số hợp lệ cho Thời lượng, Đánh giá và TMDb ID!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> authors = Arrays.asList(authorsStr.split(",\\s*"));
        List<String> actors = Arrays.asList(actorsStr.split(",\\s*"));

        Map<String, Object> movieData = new HashMap<>();
        movieData.put("title", title);
        movieData.put("genre", genre);
        movieData.put("duration", duration);
        movieData.put("posterUrl", posterUrl);
        movieData.put("description", description);
        movieData.put("rating", rating);
        movieData.put("releaseDate", releaseDate);
        movieData.put("trailerUrl", trailerUrl);
        movieData.put("authors", authors);
        movieData.put("actors", actors);
        movieData.put("country", country);
        movieData.put("tmdbId", tmdbId);

        if (movieId == null) {
            addMovie(movieData);
        } else {
            updateMovie(movieId, movieData);
        }
    }

    private void addMovie(Map<String, Object> movieData) {
        // Get the current number of movies from Firestore
        db.collection("movies").get().addOnSuccessListener(queryDocumentSnapshots -> {
            // Generate the new movie ID
            int newMovieId = queryDocumentSnapshots.size() + 1;

            // Add the id field to the movie data
            movieData.put("id", newMovieId); // Adding the custom ID to the movie data

            // Save the movie data to Firestore with the custom ID
            db.collection("movies").document(String.valueOf(newMovieId)).set(movieData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Thêm phim thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);  // Gửi kết quả về MainActivity
                        finish();  // Đóng Activity
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi thêm phim!", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi lấy danh sách phim!", Toast.LENGTH_SHORT).show());
    }

    private void updateMovie(String movieId, Map<String, Object> movieData) {
        db.collection("movies").document(movieId).update(movieData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật phim thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);  // <-- thêm dòng này
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật phim!", Toast.LENGTH_SHORT).show());
    }

}




