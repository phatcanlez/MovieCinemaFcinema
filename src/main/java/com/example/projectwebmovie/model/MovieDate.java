package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MOVIETHEATER_MOVIE_DATE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDate {
    @EmbeddedId
    private MovieDateId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("movieId")
    @JoinColumn(name = "MOVIE_ID", insertable = false, updatable = false)
    private Movie movie; // Phim liên quan, quan hệ nhiều-một

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("showDateId")
    @JoinColumn(name = "SHOW_DATE_ID", insertable = false, updatable = false)
    private ShowDates showDates; // Ngày chiếu liên quan, quan hệ nhiều-một
}