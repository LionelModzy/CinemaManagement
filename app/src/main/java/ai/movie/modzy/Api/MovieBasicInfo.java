package ai.movie.modzy.Api;

public class MovieBasicInfo {
    private int id;
    private String title;
    private String overview;
    private String poster_path;
    private String release_date;
    private double vote_average;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public String getPosterPath() { return "https://image.tmdb.org/t/p/w500" + poster_path; }
    public String getReleaseDate() { return release_date; }
    public double getVoteAverage() { return vote_average; }
}

