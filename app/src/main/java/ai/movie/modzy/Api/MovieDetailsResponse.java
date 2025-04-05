package ai.movie.modzy.Api;

import java.util.List;

public class MovieDetailsResponse {
    private int id;
    private int runtime;
    private List<Genre> genres;
    private List<ProductionCountry> production_countries;  // Thêm danh sách quốc gia sản xuất

    public int getId() { return id; }
    public int getRuntime() { return runtime; }

    public String getGenres() {
        StringBuilder genreString = new StringBuilder();
        for (Genre genre : genres) {
            genreString.append(genre.getName()).append(", ");
        }
        return genreString.length() > 0 ? genreString.substring(0, genreString.length() - 2) : "";
    }

    // Thêm phương thức lấy quốc gia sản xuất
    public String getCountry() {
        if (production_countries != null && !production_countries.isEmpty()) {
            return production_countries.get(0).getName();  // Lấy quốc gia đầu tiên
        }
        return "Unknown";
    }

    public static class Genre {
        private String name;
        public String getName() { return name; }
    }

    // Thêm class để ánh xạ dữ liệu quốc gia sản xuất
    public static class ProductionCountry {
        private String name;
        public String getName() { return name; }
    }
}
