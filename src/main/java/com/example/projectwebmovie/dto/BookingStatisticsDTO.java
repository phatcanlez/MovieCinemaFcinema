package com.example.projectwebmovie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatisticsDTO {
    private Long totalBookings;
    private Long successfulBookings;
    private Long pendingBookings;
    private Long cancelledBookings;
}