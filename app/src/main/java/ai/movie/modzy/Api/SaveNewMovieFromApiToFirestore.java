package ai.movie.modzy.Api;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import java.util.concurrent.atomic.AtomicInteger;
public class SaveNewMovieFromApiToFirestore extends AppCompatActivity {
    private static final String API_KEY = "e6d875479f6aa8eab855a247d276963a";
    private FirebaseFirestore db;
    private MovieApiService apiService;
    private List<MovieBasicInfo> moviesToSave = new ArrayList<>();
    private int currentMovieId = 1;  // Biến quản lý ID phim
    private static final int MOVIES_PER_RUN = 20;  // Số phim tối đa mỗi lần chạy
    private int moviesAddedCount = 0;  // Số phim đã thêm
    private AtomicInteger totalMoviesFetched = new AtomicInteger(0);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_new_movie_from_api_to_firestore);

        db = FirebaseFirestore.getInstance();
        apiService = ApiClient.getClient().create(MovieApiService.class);

        getLastMovieCountAndFetchMovies();
    }

    private void fetchAndSaveMovies() {
        int startingMovieIndex = currentMovieId - 1; // Lấy số lượng phim đã có trong Firestore
        int startingPage = (startingMovieIndex / 20) + 1; // Tính trang TMDb cần bắt đầu
        AtomicInteger moviesFetchedThisRun = new AtomicInteger(0); // Đếm số phim đã lấy lần này

        for (int page = startingPage; page <= 5; page++) {
            if (moviesFetchedThisRun.get() >= MOVIES_PER_RUN) {
                Log.d("DEBUG", "Reached the limit of " + MOVIES_PER_RUN + " movies. Stopping fetch.");
                break;
            }

            final int currentPage = page;
            Call<TMDbResponse> call = apiService.getPopularMovies(API_KEY, "vi-VN", page);

            call.enqueue(new Callback<TMDbResponse>() {
                @Override
                public void onResponse(@NonNull Call<TMDbResponse> call, @NonNull Response<TMDbResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<MovieBasicInfo> movies = response.body().getResults();
                        Log.d("DEBUG", "Movies fetched from page " + currentPage + ": " + movies.size());

                        for (MovieBasicInfo movieInfo : movies) {
                            if (moviesFetchedThisRun.get() < MOVIES_PER_RUN) {
                                checkIfMovieExists(movieInfo);
                                moviesFetchedThisRun.incrementAndGet();
                            } else {
                                break;
                            }
                        }
                    } else {
                        Log.e("API_ERROR", "Error fetching popular movies from page " + currentPage + ": " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<TMDbResponse> call, @NonNull Throwable t) {
                    Log.e("API_ERROR", "Failed to fetch movies from page " + currentPage + ": " + t.getMessage());
                }
            });
        }
    }




    private Set<Integer> checkedMovies = new HashSet<>(); // Set để lưu các ID phim đã kiểm tra

    private void checkIfMovieExists(MovieBasicInfo movieInfo) {
        if (checkedMovies.contains(movieInfo.getId())) {
            Log.d("Firestore", "Skipped movie (already checked): " + movieInfo.getTitle());
            return;  // Phim đã được kiểm tra, bỏ qua
        }

        db.collection("movies")
                .whereEqualTo("tmdbId", movieInfo.getId())  // Kiểm tra trùng lặp theo tmdbId
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Log.d("Firestore", "Skipped movie (already exists): " + movieInfo.getTitle());
                    } else {
                        // Nếu phim chưa tồn tại, thêm vào danh sách để lưu
                        moviesToSave.add(movieInfo);
                        moviesAddedCount++;  // Tăng số lượng phim đã thêm
                    }

                    checkedMovies.add(movieInfo.getId()); // Đánh dấu phim đã kiểm tra
                    saveNextMovie();  // Tiến hành lưu phim tiếp theo
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error checking movie existence", e));
    }

    private void saveNextMovie() {
        if (moviesToSave.isEmpty()) return; // Không còn phim để lưu

        MovieBasicInfo movieInfo = moviesToSave.remove(0);  // Lấy phim tiếp theo trong danh sách
        fetchMovieDetailsAndSave(movieInfo);  // Lấy thông tin chi tiết của phim
    }
    private void getLastMovieCountAndFetchMovies() {
        db.collection("movies")
                .orderBy("id", Query.Direction.DESCENDING) // Lấy phim có ID lớn nhất
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        currentMovieId = task.getResult().getDocuments().get(0).getLong("id").intValue() + 1;
                    } else {
                        currentMovieId = 1; // Nếu chưa có phim nào, bắt đầu từ ID 1
                    }
                    Log.d("Firestore", "Starting from movie ID: " + currentMovieId);
                    fetchAndSaveMovies(); // Gọi fetch phim từ TMDb sau khi có ID đúng
                });
    }


    private void fetchMovieDetailsAndSave(MovieBasicInfo movieInfo) {
        Call<MovieDetailsResponse> call = apiService.getMovieDetails(movieInfo.getId(), API_KEY, "vi-VN");

        call.enqueue(new Callback<MovieDetailsResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieDetailsResponse> call, @NonNull Response<MovieDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetailsResponse details = response.body();
                    fetchMovieCreditsAndSave(movieInfo, details);
                } else {
                    Log.e("API_ERROR", "Error fetching movie details: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieDetailsResponse> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Failed to fetch movie details: " + t.getMessage());
            }
        });
    }

    private void fetchMovieCreditsAndSave(MovieBasicInfo movieInfo, MovieDetailsResponse details) {
        Call<MovieCreditsResponse> call = apiService.getMovieCredits(movieInfo.getId(), API_KEY);

        call.enqueue(new Callback<MovieCreditsResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieCreditsResponse> call, @NonNull Response<MovieCreditsResponse> response) {
                List<String> directors = new ArrayList<>();
                List<String> actors = new ArrayList<>();

                if (response.isSuccessful() && response.body() != null) {
                    MovieCreditsResponse credits = response.body();

                    for (MovieCreditsResponse.Crew crewMember : credits.getCrew()) {
                        if ("Director".equalsIgnoreCase(crewMember.getJob())) {
                            directors.add(crewMember.getName());
                        }
                    }

                    for (int i = 0; i < Math.min(5, credits.getCast().size()); i++) {
                        actors.add(credits.getCast().get(i).getName());
                    }
                }

                fetchMovieTrailerAndSave(movieInfo, details, directors, actors);
            }

            @Override
            public void onFailure(@NonNull Call<MovieCreditsResponse> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Failed to fetch movie credits: " + t.getMessage());
            }
        });
    }

    private void fetchMovieTrailerAndSave(MovieBasicInfo movieInfo, MovieDetailsResponse details, List<String> directors, List<String> actors) {
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
                    fetchEnglishTrailer(movieInfo, details, directors, actors);
                } else {
                    saveMovieToFirestore(movieInfo, details, trailerUrl, directors, actors);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MovieVideosResponse> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Failed to fetch movie trailer: " + t.getMessage());
            }
        });
    }

    private void fetchEnglishTrailer(MovieBasicInfo movieInfo, MovieDetailsResponse details, List<String> directors, List<String> actors) {
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

                saveMovieToFirestore(movieInfo, details, trailerUrl, directors, actors);
            }

            @Override
            public void onFailure(@NonNull Call<MovieVideosResponse> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Failed to fetch EN movie trailer: " + t.getMessage());
            }
        });
    }

    private void saveMovieToFirestore(MovieBasicInfo movieInfo, MovieDetailsResponse details, String trailerUrl, List<String> directors, List<String> actors) {
        int newId = currentMovieId++; // Cập nhật ID phim mới (tăng ID cho mỗi phim mới)

        Movies movie = new Movies(
                newId,
                movieInfo.getTitle(),
                details.getGenres(),
                details.getRuntime(),
                movieInfo.getPosterPath(),
                movieInfo.getOverview(),
                movieInfo.getVoteAverage(),
                movieInfo.getReleaseDate(),
                trailerUrl,
                directors,
                actors,
                details.getCountry(),
                movieInfo.getId() // Lưu thêm tmdbId
        );

        db.collection("movies").document(String.valueOf(newId))
                .set(movie)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Movie added with ID: " + newId + " - " + movie.getTitle());
                    saveNextMovie(); // Tiếp tục lưu phim tiếp theo
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding movie", e));
    }


}
