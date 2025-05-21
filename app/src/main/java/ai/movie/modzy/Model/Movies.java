package ai.movie.modzy.Model;

import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class Movies {
    private int id;
    private String title;
    private String genre;
    private int duration;
    private String posterUrl;
    private String description;
    private double rating;
    private String releaseDate;
    private String trailerUrl;
    private List<String> authors;  // Đạo diễn
    private List<String> actors;    // Diễn viên chính
    private String country;
    private int tmdbId;
    @PropertyName("MovieID")
    private Long movieID;

    private List<String> tags;
    // Constructor rỗng (Firestore yêu cầu)
    public Movies() {}

    public Movies(int id, String title, String genre, int duration, String posterUrl, String description, double rating, String releaseDate, String trailerUrl,
                  List<String> authors, List<String> actors, String country,int tmdbId) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.posterUrl = posterUrl;
        this.description = description;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.trailerUrl = trailerUrl;
        this.authors = authors;
        this.actors = actors;
        this.country = country;
        this.tmdbId = tmdbId;

    }
    @PropertyName("MovieID")
    public Long getMovieID() { return movieID; }

    @PropertyName("MovieID")
    public void setMovieID(Long movieID) { this.movieID = movieID; }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getDuration() { return duration; }
    public String getPosterUrl() { return posterUrl; }
    public String getDescription() { return description; }
    public double getRating() { return rating; }
    public String getReleaseDate() { return releaseDate; }
    public String getTrailerUrl() { return trailerUrl; }
    public List<String> getAuthors() { return authors; }
    public String getCountry() { return country; }
    public List<String> getActors() { return actors; }

    public int getTmdbId() {
        return tmdbId;
    }

    public void setTmdbId(int tmdbId) {
        this.tmdbId = tmdbId;
    }
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public void setDescription(String description) { this.description = description; }
    public void setRating(double rating) { this.rating = rating; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public void setAuthors(List<String> authors) { this.authors = authors;}
    public void setCountry(String country) { this.country = country; }
    public void setActors(List<String> actors) { this.actors = actors; }
}
