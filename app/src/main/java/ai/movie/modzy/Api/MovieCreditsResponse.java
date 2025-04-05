package ai.movie.modzy.Api;

import java.util.List;

public class MovieCreditsResponse {
    private List<Cast> cast;
    private List<Crew> crew;

    public List<Cast> getCast() { return cast; }
    public List<Crew> getCrew() { return crew; }

    public static class Cast {
        private String name;
        public String getName() { return name; }
    }

    public static class Crew {
        private String name;
        private String job;
        public String getName() { return name; }
        public String getJob() { return job; }
    }
}
