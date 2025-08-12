package com.example.projectwebmovie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatisticsDTO {
    private String period;
    private Double totalRevenue;
    private Long totalBookings;
    private String revenueStatus; // "increase", "decrease", "no_change"
    private Double revenueChangePercent;
    private String bookingStatus; // "increase", "decrease", "no_change"
    private Double bookingChangePercent;

    // Constructor for backward compatibility
    public RevenueStatisticsDTO(String period, Double totalRevenue, Long totalBookings) {
        this.period = period;
        this.totalRevenue = totalRevenue;
        this.totalBookings = totalBookings;
        this.revenueStatus = "no_change";
        this.revenueChangePercent = 0.0;
        this.bookingStatus = "no_change";
        this.bookingChangePercent = 0.0;
    }
}