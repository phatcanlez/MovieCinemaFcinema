package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "MOVIETHEATER_SCHEDULE_SEAT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSeat {
    @Id
    @Column(name = "SCHEDULE_SEAT_ID", length = 10)
    private String scheduleSeatId;

    @Column(name = "MOVIE_ID", length = 10)
    private String movieId;

    @Column(name = "SCHEDULE_ID")
    private Integer scheduleId;

    @Column(name = "SEAT_ID")
    private Integer seatId;

    @Column(name = "SEAT_COLUMN", length = 255)
    private String seatColumn;

    @Column(name = "SEAT_ROW")
    private Integer seatRow;

    @Column(name = "SEAT_STATUS")
    private Integer seatStatus;

    @Column(name = "SEAT_PRICE")
    private Double seatPrice;

    @Column(name = "SHOW_DATE_ID") // Thêm trường mới
    private Integer showDateId;

    @Column(name = "CINEMA_ROOM_ID") // Thêm trường mới
    private Integer cinemaRoomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEAT_TYPE_ID", referencedColumnName = "ID")
    private SeatType seatType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MOVIE_ID", insertable = false, updatable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_ID", insertable = false, updatable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEAT_ID", insertable = false, updatable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SHOW_DATE_ID", referencedColumnName = "SHOW_DATE_ID", insertable = false, updatable = false)
    private ShowDates showDates;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CINEMA_ROOM_ID", referencedColumnName = "CINEMA_ROOM_ID", insertable = false, updatable = false)
    private CinemaRoom cinemaRoom;

    @OneToMany(mappedBy = "scheduleSeat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingSeat> bookingSeats;

    @Override
    public String toString() {
        return "ScheduleSeat{scheduleSeatId='" + scheduleSeatId + "'}";
    }
}