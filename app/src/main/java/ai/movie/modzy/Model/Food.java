package ai.movie.modzy.Model;

import java.util.List;

public class Food {
    private String id;
    private String name;
    private double price;
    private String imageUrl;
    private boolean combo;
    private List<String> comboItems;
    private String imagePublicId;

    public Food() {
        // Constructor rỗng cho Firestore
    }

    // Constructor đầy đủ dùng khi KHÔNG phải combo
    public Food(String id, String name, double price, String imageUrl, boolean combo, String imagePublicId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.combo = combo;
        this.imagePublicId = imagePublicId;
    }

    // Constructor đầy đủ dùng khi LÀ combo
    public Food(String id, String name, double price, String imageUrl, boolean combo, String imagePublicId, List<String> comboItems) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.combo = combo;
        this.imagePublicId = imagePublicId;
        this.comboItems = comboItems;
    }

    // Getters và Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isCombo() { return combo; }
    public void setCombo(boolean combo) { this.combo = combo; }

    public List<String> getComboItems() { return comboItems; }
    public void setComboItems(List<String> comboItems) { this.comboItems = comboItems; }

    public String getImagePublicId() { return imagePublicId; }
    public void setImagePublicId(String imagePublicId) { this.imagePublicId = imagePublicId; }
}
