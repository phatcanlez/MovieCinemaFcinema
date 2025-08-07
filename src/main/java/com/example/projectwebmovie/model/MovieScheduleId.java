package com.example.projectwebmovie.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class MovieScheduleId implements Serializable {

    @Column(name = "MOVIE_ID")
    private String movieId;

    @Column(name = "SCHEDULE_ID")
    private Integer scheduleId;

    @Column(name = "CINEMA_ROOM_ID")
    private Integer cinemaRoomId;

    @Column(name = "SHOW_DATE_ID")
    private Integer showDateId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieScheduleId)) return false;
        MovieScheduleId that = (MovieScheduleId) o;
        return Objects.equals(movieId, that.movieId) &&
                Objects.equals(scheduleId, that.scheduleId) &&
                Objects.equals(cinemaRoomId, that.cinemaRoomId) &&
                Objects.equals(showDateId, that.showDateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, scheduleId, cinemaRoomId, showDateId);
    }

    @Override
    public String toString() {
        return "MovieScheduleId{" +
                "movieId='" + movieId + '\'' +
                ", scheduleId=" + scheduleId +
                ", cinemaRoomId=" + cinemaRoomId +
                ", showDateId=" + showDateId +
                '}';
    }
}