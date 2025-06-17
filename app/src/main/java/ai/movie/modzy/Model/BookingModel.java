package ai.movie.modzy.Model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.List;
import java.util.Map;

public class BookingModel {
    private String bookingId;
    private String userId;
    private String showtimeId;
    private List<String> seats;
    private int totalPrice;
    private List<Map<String, Object>> foods;
    private @ServerTimestamp Timestamp bookingTime;
    private String paymentStatus;
    private String paymentMethod;

    // Thêm các trường tham chiếu
    private String movieTitle;
    private String cinemaName;
    private Timestamp showtime;

    public BookingModel() {}

    // Getter và Setter
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getShowtimeId() { return showtimeId; }
    public void setShowtimeId(String showtimeId) { this.showtimeId = showtimeId; }
    public List<String> getSeats() { return seats; }
    public void setSeats(List<String> seats) { this.seats = seats; }
    public int getTotalPrice() { return totalPrice; }
    public void setTotalPrice(int totalPrice) { this.totalPrice = totalPrice; }
    public List<Map<String, Object>> getFoods() { return foods; }
    public void setFoods(List<Map<String, Object>> foods) { this.foods = foods; }
    public Timestamp getBookingTime() { return bookingTime; }
    public void setBookingTime(Timestamp bookingTime) { this.bookingTime = bookingTime; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public String getCinemaName() { return cinemaName; }
    public void setCinemaName(String cinemaName) { this.cinemaName = cinemaName; }
    public Timestamp getShowtime() { return showtime; }
    public void setShowtime(Timestamp showtime) { this.showtime = showtime; }
}