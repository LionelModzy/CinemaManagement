package ai.movie.modzy.Api;

import java.util.List;

public class MovieDetailsResponse {
    private int id;  // Thêm ID
    private int runtime;
    private List<Genre> genres;

    public int getId() { return id; }  // Thêm getter cho ID

    public int getRuntime() { return runtime; }

    public String getGenres() {
        StringBuilder genreString = new StringBuilder();
        for (Genre genre : genres) {
            genreString.append(genre.getName()).append(", ");
        }
        return genreString.length() > 0 ? genreString.substring(0, genreString.length() - 2) : "";
    }

    public static class Genre {
        private String name;
        public String getName() { return name; }
    }
}
