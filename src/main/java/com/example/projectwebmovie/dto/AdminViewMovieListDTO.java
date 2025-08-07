package com.example.projectwebmovie.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AdminViewMovieListDTO {
    private String movieId;
    private Integer duration;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String actor;
    private String director;
    private String content;
    private String version;
    private String movieNameEnglish;
    private String movieNameVn;
    private String smallImage;
    private String largeImage;
    private Integer rating;
    private String status;
    private String trailerId;
    private String movieProductionCompany;
    private Double price; // Giá vé của phim (bổ sung)
    private List<Integer> selectedTypeIds; // Dùng để binding form (edit/add)
}
