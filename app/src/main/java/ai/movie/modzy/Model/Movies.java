package ai.movie.modzy.Model;


public class Movies {
    private int id;
    private String title;
    private String genre;
    private int duration;
    private String posterUrl;
    private String description;
    private double rating;
    private String trailerUrl;

    // Constructor rỗng (Firestore yêu cầu)
    public Movies() {}

    public Movies(int id, String title, String genre, int duration, String posterUrl, String description, double rating, String trailerUrl) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.posterUrl = posterUrl;
        this.description = description;
        this.rating = rating;
        this.trailerUrl = trailerUrl;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public int getDuration() { return duration; }
    public String getPosterUrl() { return posterUrl; }
    public String getDescription() { return description; }
    public double getRating() { return rating; }
    public String getTrailerUrl() { return trailerUrl; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public void setDescription(String description) { this.description = description; }
    public void setRating(double rating) { this.rating = rating; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
}

