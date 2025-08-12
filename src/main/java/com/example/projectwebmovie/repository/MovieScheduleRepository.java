
package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.dto.MovieRoomCalenderDTO;
import com.example.projectwebmovie.dto.MovieScheduleForBookingDTO;
import com.example.projectwebmovie.dto.MovieScheduleForShowToAdminDTO;
import com.example.projectwebmovie.model.MovieSchedule;
import com.example.projectwebmovie.model.MovieScheduleId;

import jakarta.transaction.Transactional;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface MovieScheduleRepository extends JpaRepository<MovieSchedule, MovieScheduleId> {

  /**
   * Lấy danh sách lịch chiếu theo ngày (LocalDate), trả về MovieRoomCalenderDTO
   */
  @Query("""
          SELECT new com.example.projectwebmovie.dto.MovieRoomCalenderDTO(
              ms.movie.movieNameVn,
              ms.cinemaRoom.cinemaRoomName,
              CAST(ms.showDates.showDate AS string),
              ms.schedule.scheduleTime,
              null,
              ms.movie.movieId,
              CAST(ms.schedule.scheduleId AS string),
              CAST(ms.showDates.showDateId AS string)
          )
          FROM MovieSchedule ms
          WHERE ms.showDates.showDate = :showDate
      """)
  List<MovieRoomCalenderDTO> findMovieRoomCalenderByShowDate(@Param("showDate") LocalDate showDate);

  // Tìm lịch chiếu theo ID phim, ID phòng, và ID ngày chiếu
  List<MovieSchedule> findByMovie_MovieIdAndCinemaRoom_CinemaRoomIdAndShowDates_ShowDateId(
      String movieId, Integer roomId, Integer showDateId);

  /*
   * Lấy danh sách lịch chiếu của một phòng cụ thể trong một ngày cụ thể
   * Trả về DTO gồm: giờ chiếu, tên phim, thông tin phim
   */
  @Query("""
      SELECT new com.example.projectwebmovie.dto.MovieScheduleForShowToAdminDTO
      (
          ms
      )
      FROM MovieSchedule ms
      WHERE ms.cinemaRoom.cinemaRoomId = :cinemaRoomId
      AND ms.showDates.showDate = :showDate
      """)
  List<MovieScheduleForShowToAdminDTO> findSchedulesByRoomAndDate(
      @Param("cinemaRoomId") Integer cinemaRoomId,
      @Param("showDate") LocalDate showDate);

  // Tìm lịch chiếu theo ID phim, ID phòng, và ngày chiếu (dùng LocalDate)
  List<MovieSchedule> findByMovie_MovieIdAndCinemaRoom_CinemaRoomIdAndShowDates_ShowDate(
      String movieId, int roomId, LocalDate showDate);

  List<MovieSchedule> findByCinemaRoom_CinemaRoomIdAndShowDates_ShowDate(
      int roomId, LocalDate showDate);

  /*
   * Lấy các khung giờ chiếu của một phim trong một ngày cụ thể
   * Trả về DTO chỉ chứa scheduleTime (dùng khi khách đặt vé)
   */
  @Query("""
      SELECT new com.example.projectwebmovie.dto.MovieScheduleForBookingDTO(ms.schedule.scheduleTime)
      FROM MovieSchedule ms
      WHERE ms.movie.id = :movieId AND ms.showDates.showDate = :showDate
      """)
  List<MovieScheduleForBookingDTO> findScheduleTimesByMovieIdAndShowDate(
      @Param("movieId") String movieId,
      @Param("showDate") LocalDate showDate);

  // Tìm toàn bộ lịch chiếu của một phim vào một ngày cụ thể (bỏ qua phòng)
  List<MovieSchedule> findByMovieMovieIdAndShowDatesShowDate(
      String movieId, LocalDate showDate);

  // Tìm lịch chiếu cụ thể theo movieId, roomId, showDateId và scheduleId
  MovieSchedule findByMovie_MovieIdAndCinemaRoom_CinemaRoomIdAndShowDates_ShowDateIdAndSchedule_ScheduleId(
      String movieId, int roomId, Integer showDateId, Integer scheduleId);

  // Tìm phòng chiếu theo ID phim, ID ngày chiếu và ID lịch chiếu
  MovieSchedule findByMovie_MovieIdAndShowDates_ShowDateIdAndSchedule_ScheduleId(
      String movieId, Integer showDateId, Integer scheduleId);

  /*
   * Kiểm tra các lịch chiếu bị xung đột:
   * - Cùng ngày chiếu và giờ chiếu (scheduleId)
   * - Nhưng khác phòng hoặc khác phim
   * => Trả về danh sách scheduleId bị trùng
   */
  @Query("""
      SELECT ms.schedule.scheduleId
      FROM MovieSchedule ms
      WHERE ms.showDates.showDateId = :showDateId
        AND ms.schedule.scheduleId IN :scheduleIds
        AND (
          (ms.cinemaRoom.cinemaRoomId = :roomId AND ms.movie.movieId <> :movieId)
          OR
          (ms.movie.movieId = :movieId AND ms.cinemaRoom.cinemaRoomId <> :roomId)
        )
      """)
  List<Integer> findConflictedScheduleIds(
      @Param("roomId") Integer roomId,
      @Param("movieId") String movieId,
      @Param("showDateId") Integer showDateId,
      @Param("scheduleIds") Collection<Integer> scheduleIds);

  // Phương thức findById mặc định từ JpaRepository
  Optional<MovieSchedule> findById(MovieScheduleId id);

  List<MovieSchedule> findByMovie_MovieIdAndShowDates_ShowDate(String movieId, LocalDate showDate);

  @Query("SELECT ms FROM MovieSchedule ms " +
      "JOIN FETCH ms.movie m " +
      "JOIN FETCH ms.schedule s " +
      "JOIN FETCH ms.showDates sd " +
      "JOIN FETCH ms.cinemaRoom cr " +
      "WHERE m.movieId = :movieId AND s.scheduleId = :scheduleId AND sd.showDateId = :showDateId")
  MovieSchedule findFullMovieSchedules(@Param("movieId") String movieId,
      @Param("scheduleId") int scheduleId,
      @Param("showDateId") int showDateId);

  @Query("SELECT DISTINCT sd.showDate " +
      "FROM MovieSchedule ms " +
      "JOIN ms.showDates sd " +
      "WHERE ms.movie.movieId = :movieId")
  List<LocalDate> findShowDatesByMovieId(@Param("movieId") String movieId);

  @Query(value = """
          SELECT b.schedule_id
          FROM movietheater_booking b
          JOIN movietheater_show_dates sd
            ON b.show_date_id = sd.show_date_id
          WHERE sd.show_date = :showDate
            AND b.movie_id = :movieId
            AND b.booking_status = 'SUCCESS'
      """, nativeQuery = true)
  List<Integer> findScheduleIdsByDateAndMovie(
      @Param("showDate") LocalDate showDate,
      @Param("movieId") String movieId);

  // Tìm tất cả record MovieSchedule theo cinemaRoomId
  List<MovieSchedule> findByCinemaRoom_CinemaRoomId(Integer cinemaRoomId);

}