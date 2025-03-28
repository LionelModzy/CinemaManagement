package ai.movie.modzy.Api;

import java.util.List;

public class MovieVideosResponse {
    private List<VideoInfo> results;

    public List<VideoInfo> getResults() {
        return results;
    }

    public static class VideoInfo {
        private String key;
        private String site;
        private String type;

        public String getKey() {
            return key;
        }

        public String getSite() {
            return site;
        }

        public String getType() {
            return type;
        }
    }
}
