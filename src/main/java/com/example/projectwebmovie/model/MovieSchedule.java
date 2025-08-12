package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MOVIETHEATER_MOVIE_SCHEDULE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieSchedule {

    @EmbeddedId
    private MovieScheduleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("movieId")
    @JoinColumn(name = "MOVIE_ID")
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("scheduleId")
    @JoinColumn(name = "SCHEDULE_ID")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cinemaRoomId")
    @JoinColumn(name = "CINEMA_ROOM_ID")
    private CinemaRoom cinemaRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("showDateId")
    @JoinColumn(name = "SHOW_DATE_ID")
    private ShowDates showDates;

    @Override
    public String toString() {
        return "MovieSchedule{id=" + id + "}";
    }
}
