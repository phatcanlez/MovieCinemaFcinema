package com.example.projectwebmovie.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingSeatId implements Serializable {
    private String bookingId;
    private String scheduleSeatId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingSeatId that = (BookingSeatId) o;
        return Objects.equals(bookingId, that.bookingId) &&
                Objects.equals(scheduleSeatId, that.scheduleSeatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, scheduleSeatId);
    }
}