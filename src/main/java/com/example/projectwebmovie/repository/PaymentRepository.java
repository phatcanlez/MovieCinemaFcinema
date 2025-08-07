package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    // Tìm payment mới nhất dựa trên bookingId
    @Query("SELECT p FROM Payment p WHERE p.bookingId = :bookingId ORDER BY p.paymentDate DESC LIMIT 1")
    Optional<Payment> findTopByBookingIdOrderByPaymentDateDesc(@Param("bookingId") String bookingId);

    // Tìm payment dựa trên bookingId, sắp xếp theo paymentDate giảm dần
    @Query("SELECT p FROM Payment p WHERE p.bookingId = :bookingId ORDER BY p.paymentDate DESC")
    List<Payment> findByBookingIdOrderByPaymentDateDesc(@Param("bookingId") String bookingId);

    // Tìm payment dựa trên paymentId
    Optional<Payment> findByPaymentId(@Param("paymentId") String paymentId);

    // Tìm tất cả payment trong một khoảng thời gian
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByPaymentDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Kiểm tra tồn tại payment với bookingId và trạng thái
    boolean existsByBookingIdAndStatus(@Param("bookingId") String bookingId, @Param("status") String status);
}