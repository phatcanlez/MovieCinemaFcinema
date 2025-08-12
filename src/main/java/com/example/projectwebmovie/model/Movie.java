package com.example.projectwebmovie.model;

import com.example.projectwebmovie.enums.MovieStatus;
import com.example.projectwebmovie.enums.MovieVersion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "MOVIETHEATER_MOVIE")
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @Column(name = "MOVIE_ID", length = 10)
    private String movieId;

    @Column(name = "ACTOR", length = 255)
    private String actor;

    @Column(name = "CONTENT", length = 1000)
    private String content;

    @Column(name = "DIRECTOR", length = 255)
    private String director;

    @Column(name = "DURATION")
    private Integer duration;

    @Column(name = "FROM_DATE")
    private LocalDate fromDate;

    @Column(name = "MOVIE_PRODUCTION_COMPANY", length = 255)
    private String movieProductionCompany;

    @Column(name = "TO_DATE")
    private LocalDate toDate;

    @Column(name = "VERSION", length = 255)
    @Enumerated(EnumType.STRING)
    private MovieVersion version;

    @Column(name = "MOVIE_NAME_ENGLISH", length = 255)
    private String movieNameEnglish;

    @Column(name = "MOVIE_NAME_VN", length = 255)
    private String movieNameVn;

    @Column(name = "LARGE_IMAGE", length = 255)
    private String largeImage;

    @Column(name = "SMALL_IMAGE", length = 255)
    private String smallImage;

    @Column(name = "RATING")
    private Integer rating;

    @Column(name = "STATUS", length = 20)
    @Enumerated(EnumType.STRING)
    private MovieStatus status;

    @Column(name = "TRAILER_ID", length = 255)
    private String trailerId;

    @Column(name = "PRICE")
    private Double price;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MovieSchedule> movieSchedules;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MovieType> movieTypes;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MovieDate> movieDates;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScheduleSeat> scheduleSeats;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings;

    @Override
    public String toString() {
        return "Movie{movieId='" + movieId + "', movieNameVn='" + movieNameVn + "', status=" + status + "}";
    }
}