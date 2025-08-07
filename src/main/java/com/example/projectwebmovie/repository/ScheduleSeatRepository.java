package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.ScheduleSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleSeatRepository extends JpaRepository<ScheduleSeat, String> {

    @Query("SELECT ss FROM ScheduleSeat ss WHERE ss.movieId = :movieId AND ss.scheduleId = :scheduleId AND ss.showDateId = :showDateId ORDER BY ss.scheduleSeatId ASC")
    List<ScheduleSeat> findByMovieIdAndScheduleIdAndShowDateId(
            @Param("movieId") String movieId,
            @Param("scheduleId") Integer scheduleId,
            @Param("showDateId") Integer showDateId);

    @Query("SELECT ss FROM ScheduleSeat ss WHERE ss.cinemaRoomId = :cinemaRoomId AND ss.showDateId = :showDateId")
    List<ScheduleSeat> findByCinemaRoomIdAndShowDateId(@Param("cinemaRoomId") Integer cinemaRoomId,
            @Param("showDateId") Integer showDateId);

    ScheduleSeat findBySeat_SeatIdAndSchedule_ScheduleId(Integer seatId, Integer scheduleId);

    @Query("SELECT s FROM ScheduleSeat s WHERE s.seat.seatId IN :seatIds AND s.schedule.scheduleId = :scheduleId")
    List<ScheduleSeat> findBySeat_SeatIdInAndSchedule_ScheduleId(List<Integer> seatIds, Integer scheduleId);

    @Query("SELECT ss FROM ScheduleSeat ss " +
            "JOIN FETCH ss.seat s " +
            "WHERE ss.scheduleId = :scheduleId " +
            "AND ss.seatStatus = 0 " + // Chỉ lấy ghế có seat_status = 0
            "AND s.isActive = true " +
            "AND ss.movieId = :movieId " +
            "AND ss.showDateId = :showDateId " +
            "ORDER BY s.seatRow, s.seatColumn")
    List<ScheduleSeat> findAvailableSeatsByScheduleId(Integer scheduleId, String movieId, Integer showDateId);

    @Query(value = """
            SELECT ss.*
            FROM movietheater_schedule_seat ss
            WHERE ss.movie_id = :movieId
              AND ss.schedule_id = :scheduleId
              AND ss.show_date_id = :showDateId
            ORDER BY ss.schedule_seat_id ASC;
            """, nativeQuery = true)
    List<ScheduleSeat> findSeatsByMovieScheduleAndShowDate(
            @Param("movieId") String movieId,
            @Param("scheduleId") Integer scheduleId,
            @Param("showDateId") Integer showDateId);

    List<ScheduleSeat> findByMovieIdAndCinemaRoomIdAndShowDateId(
            String movieId, Integer roomId, Integer showDateId);

}