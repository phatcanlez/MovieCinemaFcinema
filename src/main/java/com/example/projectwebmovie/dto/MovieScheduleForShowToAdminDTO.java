package com.example.projectwebmovie.dto;
import com.example.projectwebmovie.model.MovieSchedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@NoArgsConstructor
@Data
public class MovieScheduleForShowToAdminDTO {
    private String time;
    private String movieName;
    private String showDate; // dạng String để hiển thị
    private String movieId;
    private String scheduleId;
    private String showDateId;
    private String largeImg;

    // Constructor that matches your @Query - accepts LocalDate and converts to String
    public MovieScheduleForShowToAdminDTO(
            String time,
            String movieName,
            String showDate,  // This matches what your query passes
            String movieId,
            String scheduleId,
            String showDateId
    ) {
        this.time = time;
        this.movieName = movieName;
        this.showDate = showDate;
        this.movieId = movieId;
        this.scheduleId = scheduleId;
        this.showDateId = showDateId;
    }

    public MovieScheduleForShowToAdminDTO(MovieSchedule ms){
        this.time = ms.getSchedule().getScheduleTime();
        this.movieName = ms.getMovie().getMovieNameVn();
        this.showDate = ms.getShowDates().getShowDate().toString();
        this.movieId = ms.getMovie().getMovieId();
        this.scheduleId = ms.getSchedule().getScheduleId().toString();
        this.showDateId = ms.getShowDates().getShowDateId().toString();
        this.largeImg=ms.getMovie().getLargeImage();
    }

    // Keep your existing 3-parameter constructor
    public MovieScheduleForShowToAdminDTO(String time, String movieName, LocalDate showDate) {
        this.time = time;
        this.movieName = movieName;
        this.showDate = showDate != null ? showDate.toString() : null;
    }
}