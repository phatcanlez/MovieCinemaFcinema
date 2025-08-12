package com.example.projectwebmovie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Data
public class MyTicketDTO {
    private String bookingId;
    private String movieTitle;
    private LocalDateTime bookingDateTime;
    private String cinemaName;
    private String seats; // Comma-separated string instead of List
    private Double totalPrice;
    private String status;
    private String combos; // JSON string for combos
}