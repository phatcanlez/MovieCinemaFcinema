package com.example.projectwebmovie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopPhimHotDTO {
    private String movieId;
    private Integer duration;
    private String fromDate;
    private String toDate;
    private String actor;
    private String director;
    private String content;
    private String version;
    private String movieNameEnglish;
    private String movieNameVn;
    private Integer rating;
    private String smallImage;
    private String largeImage;
    private String status;
    private String trailerId;
    private Double price;
    private String movieProductionCompany;
    private List<String> types;
    private Long totalBookings;
    private Double totalRevenue;

    // Constructor for query
    public TopPhimHotDTO(String movieId, Integer duration, String fromDate, String toDate,
                         String actor, String director, String content, String version,
                         String movieNameEnglish, String movieNameVn, Integer rating,
                         String smallImage, String largeImage, String status, String trailerId,
                         Double price, String movieProductionCompany, Long totalBookings, Double totalRevenue) {
        this.movieId = movieId;
        this.duration = duration;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.actor = actor;
        this.director = director;
        this.content = content;
        this.version = version;
        this.movieNameEnglish = movieNameEnglish;
        this.movieNameVn = movieNameVn;
        this.rating = rating;
        this.smallImage = smallImage;
        this.largeImage = largeImage;
        this.status = status;
        this.trailerId = trailerId;
        this.price = price;
        this.movieProductionCompany = movieProductionCompany;
        this.totalBookings = totalBookings != null ? totalBookings : 0L;
        this.totalRevenue = totalRevenue != null ? totalRevenue : 0.0;
    }
}