package com.example.projectwebmovie.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieTypeId implements java.io.Serializable {
    @Column(name = "MOVIE_ID", length = 10)
    private String movieId;

    @Column(name = "TYPE_ID")
    private Integer typeId;

    // BẮT BUỘC PHẢI OVERRIDE equals() và hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieTypeId)) return false;
        MovieTypeId that = (MovieTypeId) o;
        return Objects.equals(movieId, that.movieId) &&
                Objects.equals(typeId, that.typeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(movieId, typeId);
    }
}

