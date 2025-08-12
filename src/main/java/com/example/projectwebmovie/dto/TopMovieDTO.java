package com.example.projectwebmovie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopMovieDTO {
    private String movieId;
    private String movieNameVn;
    private String movieNameEnglish;
    private String posterUrl;
    private Long totalBookings;
    private Double totalRevenue;
}