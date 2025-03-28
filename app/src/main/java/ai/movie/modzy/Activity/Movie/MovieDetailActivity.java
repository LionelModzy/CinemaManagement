package ai.movie.modzy.Activity.Movie;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import ai.movie.modzy.Model.Movies;
import ai.movie.modzy.R;

public class MovieDetailActivity extends AppCompatActivity {
    private ImageView poster;
    private TextView title, genre, duration, rating, description, releaseDate;
    private WebView youtubeWebView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // Ánh xạ view
        poster = findViewById(R.id.detail_poster);
        title = findViewById(R.id.detail_title);
        genre = findViewById(R.id.detail_genre);
        duration = findViewById(R.id.detail_duration);
        rating = findViewById(R.id.detail_rating);
        description = findViewById(R.id.detail_description);
        releaseDate = findViewById(R.id.detail_release_date);
        youtubeWebView = findViewById(R.id.youtube_webview);

        db = FirebaseFirestore.getInstance();

        int movieId = getIntent().getIntExtra("movie_id", -1);
        if (movieId != -1) {
            loadMovieDetails(movieId);
        }
    }

    private void loadMovieDetails(int movieId) {
        db.collection("movies").document(String.valueOf(movieId)).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Movies movie = documentSnapshot.toObject(Movies.class);
                        if (movie != null) {
                            title.setText(movie.getTitle());
                            genre.setText(movie.getGenre());
                            duration.setText(movie.getDuration() + " min");
                            rating.setText("Rating: " + movie.getRating());
                            description.setText(movie.getDescription());
                            releaseDate.setText("Release Date: " + movie.getReleaseDate());

                            Glide.with(this).load(movie.getPosterUrl()).into(poster);

                            // Load trailer
                            String trailerUrl = movie.getTrailerUrl();
                            if (trailerUrl != null && !trailerUrl.isEmpty()) {
                                loadYouTubeVideo(trailerUrl);
                            }
                        }
                    }
                });
    }

    private void loadYouTubeVideo(String youtubeUrl) {
        String videoId = extractYouTubeId(youtubeUrl);
        if (videoId.isEmpty()) return;

        String embedUrl = "https://www.youtube.com/embed/" + videoId + "?autoplay=1&rel=0";

        youtubeWebView.getSettings().setJavaScriptEnabled(true);
        youtubeWebView.getSettings().setDomStorageEnabled(true);
        youtubeWebView.setWebViewClient(new WebViewClient());
        youtubeWebView.loadUrl(embedUrl);
    }


    private String extractYouTubeId(String youtubeUrl) {
        String videoId = "";
        if (youtubeUrl != null) {
            String regex = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?feature=player_embedded&v=|%2Fvideos%2F|%2Fv%2F|%2Fe%2F|watch\\?v%3D|watch\\?feature=player_embedded%26v%3D|%2Fembed%2F|%2Fshorts%2F|/shorts/|youtube.com/shorts/|youtu.be/)([a-zA-Z0-9_-]{11})";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
            java.util.regex.Matcher matcher = pattern.matcher(youtubeUrl);
            if (matcher.find()) {
                videoId = matcher.group();
            }
        }
        return videoId;
    }
}
