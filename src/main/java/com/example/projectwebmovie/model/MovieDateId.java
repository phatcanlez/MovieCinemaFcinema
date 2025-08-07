package com.example.projectwebmovie.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDateId implements Serializable {
    @Column(name = "MOVIE_ID", length = 10)
    private String movieId;

    @Column(name = "SHOW_DATE_ID")
    private Integer showDateId;

    // BẮT BUỘC PHẢI OVERRIDE equals() và hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieTypeId)) return false;
        MovieDateId that = (MovieDateId) o;
        return Objects.equals(movieId, that.movieId) &&
                Objects.equals(showDateId, that.showDateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, showDateId);
    }
}

