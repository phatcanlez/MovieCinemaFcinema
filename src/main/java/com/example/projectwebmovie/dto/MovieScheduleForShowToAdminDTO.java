package com.example.projectwebmovie.dto;

import com.example.projectwebmovie.model.Movie;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@Data
@Getter
@Setter
public class MovieScheduleForShowToAdminDTO {
    private String time;
    private String movieName;
    private String largeImage;
    private String showDate;
    public MovieScheduleForShowToAdminDTO(String time, String movieName) {
        this.time = time;
        this.movieName = movieName;
    }
    public MovieScheduleForShowToAdminDTO(String time, String movieName, LocalDate showDate) {
        this.time = time;
        this.movieName = movieName;
        this.showDate = showDate.toString();
    }

    public MovieScheduleForShowToAdminDTO(String time, String movieName, Movie movie, LocalDate showDate) {
        this.time = time;
        this.movieName = movieName;
        this.largeImage = movie.getLargeImage();
        this.showDate = showDate.toString();
    }
    public MovieScheduleForShowToAdminDTO(String time, String movieName, Movie movie) {
        this.time = time;
        this.movieName = movieName;
        this.largeImage = movie.getLargeImage();
    }

}
