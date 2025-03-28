package ai.movie.modzy.Api;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import ai.movie.modzy.Api.ApiClient;
import ai.movie.modzy.Api.MovieApiService;
import ai.movie.modzy.Api.MovieBasicInfo;
import ai.movie.modzy.Api.MovieDetailsResponse;
import ai.movie.modzy.Api.TMDbResponse;
import ai.movie.modzy.Model.Movies;
import ai.movie.modzy.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SaveNewMovieFromApiToFirestore extends AppCompatActivity {
    private static final String API_KEY = "e6d875479f6aa8eab855a247d276963a";
    private FirebaseFirestore db;
    private MovieApiService apiService;
    private List<MovieBasicInfo> moviesToSave = new ArrayList<>();
    private int currentMovieId = 1;  // Biến quản lý ID phim

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_new_movie_from_api_to_firestore);

        db = FirebaseFirestore.getInstance();
        apiService = ApiClient.getClient().create(MovieApiService.class);

        fetchAndSaveMovies();
    }

    private void fetchAndSaveMovies() {
        Call<TMDbResponse> call = apiService.getPopularMovies(API_KEY, "vi-VN", 1);

        call.enqueue(new Callback<TMDbResponse>() {
            @Override
            public void onResponse(@NonNull Call<TMDbResponse> call, @NonNull Response<TMDbResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieBasicInfo> movies = response.body().getResults();
                    for (int i = 0; i < Math.min(movies.size(), 30); i++) {
                        MovieBasicInfo movieInfo = movies.get(i);
                        checkIfMovieExists(movieInfo);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TMDbResponse> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Failed to fetch movies: " + t.getMessage());
            }
        });
    }

    private void checkIfMovieExists(MovieBasicInfo movieInfo) {
        db.collection("movies")
                .whereEqualTo("tmdbId", movieInfo.getId())  // Kiểm tra dựa trên ID TMDb
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Log.d("Firestore", "Skipped movie (already exists): " + movieInfo.getTitle());
                    } else {
                        moviesToSave.add(movieInfo);
                    }

                    // Khi đã kiểm tra hết danh sách phim, lấy ID cuối cùng và bắt đầu lưu
                    if (moviesToSave.size() > 0 && task.isComplete()) {
                        getLastMovieIdAndStartSaving();
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error checking movie existence", e));
    }

    private void getLastMovieIdAndStartSaving() {
        db.collection("movies")
                .orderBy("id", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        currentMovieId = task.getResult().getDocuments().get(0).getLong("id").intValue() + 1;
                    }
                    saveNextMovie();
                });
    }

    private void saveNextMovie() {
        if (moviesToSave.isEmpty()) return; // Không còn phim để lưu

        MovieBasicInfo movieInfo = moviesToSave.remove(0);
        fetchMovieDetailsAndSave(movieInfo);
    }

    private void fetchMovieDetailsAndSave(MovieBasicInfo movieInfo) {
        Call<MovieDetailsResponse> call = apiService.getMovieDetails(movieInfo.getId(), API_KEY, "vi-VN");

        call.enqueue(new Callback<MovieDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieDetailsResponse> call, @NonNull Response<MovieDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetailsResponse details = response.body();
                    fetchMovieTrailerAndSave(movieInfo, details);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieDetailsResponse> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Failed to fetch movie details: " + t.getMessage());
            }
        });
    }

    private void fetchMovieTrailerAndSave(MovieBasicInfo movieInfo, MovieDetailsResponse details) {
        Call<MovieVideosResponse> call = apiService.getMovieVideos(movieInfo.getId(), API_KEY, "vi-VN");

        call.enqueue(new Callback<MovieVideosResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieVideosResponse> call, @NonNull Response<MovieVideosResponse> response) {
                String trailerUrl = null;

                if (response.isSuccessful() && response.body() != null) {
                    List<MovieVideosResponse.VideoInfo> videos = response.body().getResults();
                    Log.d("DEBUG_TRAILER", "Total videos found: " + videos.size());

                    // Tìm trailer YouTube
                    for (MovieVideosResponse.VideoInfo video : videos) {
                        Log.d("DEBUG_TRAILER", "Video - Key: " + video.getKey() + ", Type: " + video.getType() + ", Site: " + video.getSite());

                        if ("YouTube".equalsIgnoreCase(video.getSite()) && "Trailer".equalsIgnoreCase(video.getType())) {
                            trailerUrl = "https://www.youtube.com/watch?v=" + video.getKey();
                            break;
                        }
                    }

                    // Nếu không có trailer, lấy video đầu tiên
                    if (trailerUrl == null && !videos.isEmpty()) {
                        trailerUrl = "https://www.youtube.com/watch?v=" + videos.get(0).getKey();
                    }
                }

                // Nếu trailer vẫn null, thử lại với ngôn ngữ "en-US"
                if (trailerUrl == null) {
                    Log.e("DEBUG_TRAILER", "No trailer found in vi-VN, trying en-US...");
                    fetchEnglishTrailer(movieInfo, details);
                } else {
                    saveMovieToFirestore(movieInfo, details, trailerUrl);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieVideosResponse> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Failed to fetch movie trailer: " + t.getMessage());
            }
        });
    }

    // Gọi API lấy trailer tiếng Anh nếu tiếng Việt không có
    private void fetchEnglishTrailer(MovieBasicInfo movieInfo, MovieDetailsResponse details) {
        Call<MovieVideosResponse> call = apiService.getMovieVideos(movieInfo.getId(), API_KEY, "en-US");

        call.enqueue(new Callback<MovieVideosResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieVideosResponse> call, @NonNull Response<MovieVideosResponse> response) {
                String trailerUrl = null;

                if (response.isSuccessful() && response.body() != null) {
                    List<MovieVideosResponse.VideoInfo> videos = response.body().getResults();
                    Log.d("DEBUG_TRAILER", "EN Total videos found: " + videos.size());

                    // Tìm trailer YouTube tiếng Anh
                    for (MovieVideosResponse.VideoInfo video : videos) {
                        Log.d("DEBUG_TRAILER", "EN Video - Key: " + video.getKey() + ", Type: " + video.getType() + ", Site: " + video.getSite());

                        if ("YouTube".equalsIgnoreCase(video.getSite()) && "Trailer".equalsIgnoreCase(video.getType())) {
                            trailerUrl = "https://www.youtube.com/watch?v=" + video.getKey();
                            break;
                        }
                    }

                    // Nếu không có trailer, lấy video đầu tiên
                    if (trailerUrl == null && !videos.isEmpty()) {
                        trailerUrl = "https://www.youtube.com/watch?v=" + videos.get(0).getKey();
                    }
                }

                if (trailerUrl == null) {
                    Log.e("DEBUG_TRAILER", "No trailer found in en-US either.");
                    trailerUrl = "N/A";  // Không có trailer nào cả
                }

                saveMovieToFirestore(movieInfo, details, trailerUrl);
            }

            @Override
            public void onFailure(@NonNull Call<MovieVideosResponse> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Failed to fetch EN movie trailer: " + t.getMessage());
            }
        });
    }


    private void saveMovieToFirestore(MovieBasicInfo movieInfo, MovieDetailsResponse details, String trailerUrl) {
        int newId = currentMovieId++;

        Movies movie = new Movies(
                newId,
                movieInfo.getTitle(),
                details.getGenres(),
                details.getRuntime(),
                movieInfo.getPosterPath(),
                movieInfo.getOverview(),
                movieInfo.getVoteAverage(),
                movieInfo.getReleaseDate(),
                trailerUrl
        );


        db.collection("movies").document(String.valueOf(newId))
                .set(movie)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Movie added: " + movie.getTitle());
                    saveNextMovie(); // Tiếp tục lưu phim tiếp theo
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding movie", e));
    }
}
