package ai.movie.modzy.Model;

public class User {
    private String name;
    private String email;
    private String role;
    private String avatarUrl;
    private String avatarPublicId; // Thêm dòng này
    private String id; // UID Firestore

    public User() {} // Required by Firestore

    public User(String name, String email, String role, String avatarUrl, String avatarPublicId) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.avatarPublicId = avatarPublicId; // Khởi tạo thêm
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getAvatarPublicId() { return avatarPublicId; } // Getter mới

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setAvatarPublicId(String avatarPublicId) { this.avatarPublicId = avatarPublicId; } // Setter mới
}
