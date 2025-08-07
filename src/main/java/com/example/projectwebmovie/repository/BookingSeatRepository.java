package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.BookingSeat;
import com.example.projectwebmovie.model.BookingSeatId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, BookingSeatId> {
    List<BookingSeat> findByBookingId(String bookingId);
}
