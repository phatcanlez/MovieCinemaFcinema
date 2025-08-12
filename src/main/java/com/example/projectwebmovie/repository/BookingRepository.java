package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.dto.TopCustomerDTO;
import com.example.projectwebmovie.dto.TopMovieDTO;
import com.example.projectwebmovie.dto.TopPhimHotDTO;
import com.example.projectwebmovie.model.Booking;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, String> {
        List<Booking> findBookingsByAccountId(String accountId);

        @Query(value = "SELECT * FROM MOVIETHEATER_BOOKING WHERE account_id = :accountId ORDER BY booking_date DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
        List<Booking> findBookingsByAccountIdWithPagination(@Param("accountId") String accountId,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

        @Query("SELECT COUNT(b) FROM Booking b WHERE b.accountId = :accountId")
        Integer countBookingsByAccountId(@Param("accountId") String accountId);

        @Query(value = "SELECT b FROM Booking b " +
                        "WHERE b.accountId = :accountId AND b.promotionId = :promotionId " +
                        "ORDER BY b.bookingDate DESC" +
                        " LIMIT 1")
        Booking findLatestBookingWithPromotion(@Param("accountId") String accountId,
                        @Param("promotionId") String promotionId);

        @Query("SELECT new com.example.projectwebmovie.dto.TopCustomerDTO(" +
                        "a.accountId, a.fullName, a.email, a.phoneNumber, " +
                        "COUNT(b.bookingId), SUM(b.totalPrice)) " +
                        "FROM Booking b " +
                        "JOIN Account a ON b.accountId = a.accountId " +
                        "WHERE b.status = 'SUCCESS' AND a.accountId NOT LIKE 'GUEST_%' " +
                        "GROUP BY a.accountId, a.fullName, a.email, a.phoneNumber " +
                        "ORDER BY SUM(b.totalPrice) DESC")
        List<TopCustomerDTO> findTop5CustomersBySpending();

        @Query("SELECT new com.example.projectwebmovie.dto.TopMovieDTO(" +
                        "m.movieId, m.movieNameVn, m.movieNameEnglish, m.smallImage, " +
                        "COUNT(b.bookingId), SUM(b.totalPrice)) " +
                        "FROM Booking b " +
                        "JOIN Movie m ON b.movieId = m.movieId " +
                        "WHERE b.status = 'SUCCESS' " +
                        "GROUP BY m.movieId, m.movieNameVn, m.movieNameEnglish, m.smallImage " +
                        "ORDER BY COUNT(b.bookingId) DESC, SUM(b.totalPrice) DESC")
        List<TopMovieDTO> findTop5MoviesByBookings();

        // Customer statistics
        @Query("SELECT COUNT(DISTINCT a) FROM Account a WHERE a.roleId = 3")
        Long countTotalCustomers();

        @Query("SELECT COUNT(DISTINCT a) FROM Account a WHERE a.roleId = 3 AND a.status = 1")
        Long countActiveCustomers();

        // Movie statistics
        @Query("SELECT COUNT(m) FROM Movie m WHERE m.status = 'SHOWING'")
        Long countShowingMovies();

        @Query("SELECT COUNT(m) FROM Movie m WHERE m.status IN ('SHOWING', 'COMING_SOON')")
        Long countActiveMovies();

        // Booking statistics
        @Query("SELECT COUNT(b) FROM Booking b")
        Long countTotalBookings();

        @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'SUCCESS'")
        Long countSuccessfulBookings();

        @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'PENDING'")
        Long countPendingBookings();

        @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CANCELLED'")
        Long countCancelledBookings();

        // Revenue statistics by date
        @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b " +
                        "WHERE b.status = 'SUCCESS' AND DATE(b.bookingDate) = :date")
        Double getRevenueByDate(@Param("date") LocalDate date);

        @Query("SELECT COUNT(b) FROM Booking b " +
                        "WHERE b.status = 'SUCCESS' AND DATE(b.bookingDate) = :date")
        Long getBookingCountByDate(@Param("date") LocalDate date);

        // Revenue statistics by month
        @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b " +
                        "WHERE b.status = 'SUCCESS' AND YEAR(b.bookingDate) = :year AND MONTH(b.bookingDate) = :month")
        Double getRevenueByMonth(@Param("year") int year, @Param("month") int month);

        @Query("SELECT COUNT(b) FROM Booking b " +
                        "WHERE b.status = 'SUCCESS' AND YEAR(b.bookingDate) = :year AND MONTH(b.bookingDate) = :month")
        Long getBookingCountByMonth(@Param("year") int year, @Param("month") int month);

        // Revenue statistics by year
        @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b " +
                        "WHERE b.status = 'SUCCESS' AND YEAR(b.bookingDate) = :year")
        Double getRevenueByYear(@Param("year") int year);

        @Query("SELECT COUNT(b) FROM Booking b " +
                        "WHERE b.status = 'SUCCESS' AND YEAR(b.bookingDate) = :year")
        Long getBookingCountByYear(@Param("year") int year);

        // Revenue statistics by date range
        @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b " +
                        "WHERE b.status = 'SUCCESS' AND DATE(b.bookingDate) BETWEEN :startDate AND :endDate")
        Double getRevenueBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        @Query("SELECT COUNT(b) FROM Booking b " +
                        "WHERE b.status = 'SUCCESS' AND DATE(b.bookingDate) BETWEEN :startDate AND :endDate")
        Long getBookingCountBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

        // Get bookings by date
        @Query("SELECT b FROM Booking b WHERE DATE(b.bookingDate) = :date ORDER BY b.bookingDate DESC")
        List<Booking> findBookingsByDate(@Param("date") LocalDate date);

        // Get bookings by month
        @Query("SELECT b FROM Booking b WHERE YEAR(b.bookingDate) = :year AND MONTH(b.bookingDate) = :month ORDER BY b.bookingDate DESC")
        List<Booking> findBookingsByMonth(@Param("year") int year, @Param("month") int month);

        // Get bookings by year
        @Query("SELECT b FROM Booking b WHERE YEAR(b.bookingDate) = :year ORDER BY b.bookingDate DESC")
        List<Booking> findBookingsByYear(@Param("year") int year);

        // Get bookings between dates
        @Query("SELECT b FROM Booking b WHERE DATE(b.bookingDate) BETWEEN :startDate AND :endDate ORDER BY b.bookingDate DESC")
        List<Booking> findBookingsBetweenDates(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT new com.example.projectwebmovie.dto.TopPhimHotDTO(" +
                        "m.movieId, m.duration, " +
                        "CAST(m.fromDate AS string), CAST(m.toDate AS string), " +
                        "m.actor, m.director, m.content, " +
                        "CAST(m.version AS string), " +
                        "m.movieNameEnglish, m.movieNameVn, m.rating, " +
                        "m.smallImage, m.largeImage, " +
                        "CAST(m.status AS string), " +
                        "m.trailerId, m.price, m.movieProductionCompany, " +
                        "CAST(COALESCE(COUNT(b.bookingId), 0) AS long), " +
                        "CAST(COALESCE(SUM(b.totalPrice), 0.0) AS double)) " +
                        "FROM Movie m " +
                        "LEFT JOIN m.bookings b ON b.status = 'SUCCESS' " +
                        "GROUP BY m.movieId, m.duration, m.fromDate, m.toDate, m.actor, m.director, " +
                        "m.content, m.version, m.movieNameEnglish, m.movieNameVn, m.rating, " +
                        "m.smallImage, m.largeImage, m.status, m.trailerId, m.price, m.movieProductionCompany " +
                        "ORDER BY COUNT(b.bookingId) DESC, SUM(b.totalPrice) DESC")
        List<TopPhimHotDTO> findAllMoviesByBookingAndRevenue();

        @Query(value = "SELECT * FROM MOVIETHEATER_BOOKING ORDER BY booking_date DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
        List<Booking> findBookingsWithPagination(
                        @Param("limit") int limit,
                        @Param("offset") int offset);

        @Query(value = "SELECT DISTINCT b.* FROM MOVIETHEATER_BOOKING b " +
                        "LEFT JOIN MOVIETHEATER_MOVIE m ON b.movie_id = m.movie_id " +
                        "LEFT JOIN MOVIETHEATER_SHOW_DATES sd ON b.show_date_id = sd.show_date_id " +
                        "LEFT JOIN MOVIETHEATER_SCHEDULE s ON b.schedule_id = s.schedule_id " +
                        "LEFT JOIN MOVIETHEATER_MOVIE_SCHEDULE ms ON (ms.movie_id = m.movie_id AND ms.schedule_id = s.schedule_id) "
                        +
                        "LEFT JOIN MOVIETHEATER_CINEMA_ROOM cr ON ms.cinema_room_id = cr.cinema_room_id " +
                        "WHERE (:searchTerm IS NULL OR " +
                        "   b.booking_id LIKE CONCAT('%', :searchTerm, '%') OR " +
                        "   m.movie_name_vn LIKE CONCAT('%', :searchTerm, '%') OR " +
                        "   cr.cinema_room_name LIKE CONCAT('%', :searchTerm, '%')) " +
                        "AND (:status IS NULL OR b.booking_status = :status) " +
                        "ORDER BY b.booking_date DESC " +
                        "LIMIT :limit OFFSET :offset", nativeQuery = true)
        List<Booking> findBookingsWithFilters(
                        @Param("searchTerm") String searchTerm,
                        @Param("status") String status,
                        @Param("limit") int limit,
                        @Param("offset") int offset);

        @Query("SELECT COUNT(b) FROM Booking b WHERE " +
                        "(:searchTerm IS NULL OR UPPER(b.bookingId) LIKE %:searchTerm% OR " +
                        "UPPER(b.movie.movieNameVn) LIKE %:searchTerm%) AND " +
                        "(:status IS NULL OR CAST(b.status as string) = :status)")
        Long countBookingsBySearchTermAndStatus(
                        @Param("searchTerm") String searchTerm,
                        @Param("status") String status);
}