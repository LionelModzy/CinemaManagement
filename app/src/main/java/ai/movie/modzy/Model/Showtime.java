package ai.movie.modzy.Model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class Showtime {
    private String id;
//    private int movieID;
    @PropertyName("MovieID")
    private int movieID;
    private String cinema;
    private String room;
    private Timestamp datetime;
    private int availableSeats;
    private int price;
    private String status;
    private List<String> bookedSeats;

    public Showtime() {}

    public Showtime(String id, int movieID, String cinema, String room, Timestamp datetime, int availableSeats, int price, String status, List<String> bookedSeats) {
        this.id = id;
        this.movieID = movieID;
        this.cinema = cinema;
        this.room = room;
        this.datetime = datetime;
        this.availableSeats = availableSeats;
        this.price = price;
        this.status = status;
        this.bookedSeats = bookedSeats;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @PropertyName("MovieID")
    public int getMovieID() { return movieID; }

    @PropertyName("MovieID")
    public void setMovieID(int movieID) { this.movieID = movieID; }
    public String getCinema() { return cinema; }
    public void setCinema(String cinema) { this.cinema = cinema; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public Timestamp getDatetime() { return datetime; }
    public void setDatetime(Timestamp datetime) { this.datetime = datetime; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getBookedSeats() { return bookedSeats; }
    public void setBookedSeats(List<String> bookedSeats) { this.bookedSeats = bookedSeats; }
}
