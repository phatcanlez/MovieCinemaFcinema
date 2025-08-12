package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.BookingCombo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingComboRepository extends JpaRepository<BookingCombo, Integer> {
    // Có thể thêm các phương thức tùy chỉnh nếu cần, ví dụ:
    // List<BookingCombo> findByBookingId(String bookingId);
}