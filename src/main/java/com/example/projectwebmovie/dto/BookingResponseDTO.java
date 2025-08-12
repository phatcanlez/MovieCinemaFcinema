package com.example.projectwebmovie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private String bookingId;
    private String accountId;
    private String movieId;
    private String movieName;
    private LocalDateTime bookingDate;
    private Double totalPrice;
    private String status;
    private String cinemaName;
    private String roomName;
    private String showTime;
    private String seats;
}