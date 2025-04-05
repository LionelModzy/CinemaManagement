package ai.movie.modzy.Api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieApiService {
    @GET("movie/popular")
    Call<TMDbResponse> getPopularMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page
    );

    @GET("movie/{movie_id}") // Sửa lỗi tại đây
    Call<MovieDetailsResponse> getMovieDetails(
            @Path("movie_id") int movieId, // Thêm @Path để truyền movie_id đúng cách
            @Query("api_key") String apiKey,
            @Query("language") String language
    );
    @GET("movie/{movie_id}/videos")
    Call<MovieVideosResponse> getMovieVideos(
            @Path("movie_id") int movieId,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );
    @GET("movie/{movie_id}/credits")
    Call<MovieCreditsResponse> getMovieCredits(@Path("movie_id") int movieId, @Query("api_key") String apiKey);


}
