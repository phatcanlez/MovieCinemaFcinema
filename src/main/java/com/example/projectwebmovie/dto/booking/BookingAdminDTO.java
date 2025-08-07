package com.example.projectwebmovie.dto.booking;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingAdminDTO {
    private String bookingId;
    private String movieName;
    private LocalDate showDate;
    private LocalTime showTime;
    private String roomName;
    private Double totalPrice;
    private String status;

    public BookingAdminDTO(String bookingId, String movieName, LocalDate showDate, LocalTime showTime,
                           String roomName, Double totalPrice, String status) {
        this.bookingId = bookingId;
        this.movieName = movieName;
        this.showDate = showDate;
        this.showTime = showTime;
        this.roomName = roomName;
        this.totalPrice = totalPrice;
        this.status = status;
    }
}