package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MOVIETHEATER_MOVIE_TYPE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieType {
    @EmbeddedId
    private MovieTypeId id;

    @Column(name = "PRIMARY_TYPE")
    private Boolean primaryType; // Thể loại chính (true/false) (bổ sung)

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("movieId")
    @JoinColumn(name = "MOVIE_ID", insertable = false, updatable = false)
    private Movie movie; // Phim liên quan, quan hệ nhiều-một

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("typeId")
    @JoinColumn(name = "TYPE_ID", insertable = false, updatable = false)
    private Type type; // Thể loại liên quan, quan hệ nhiều-một
}