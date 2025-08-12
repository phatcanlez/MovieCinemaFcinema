package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.Seat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Integer> {
  /**
   * Đếm số lượng ghế đang hoạt động trong một phòng chiếu cụ thể.
   *
   * @param roomId ID của phòng chiếu
   * @return tổng số ghế đang hoạt động trong phòng chiếu
   */
  long countByCinemaRoomIdAndIsActiveTrue(Integer roomId);

  /**
   * Tìm tất cả ghế trong một phòng chiếu cụ thể.
   *
   * @param roomId ID của phòng chiếu
   * @return danh sách các ghế trong phòng chiếu
   */
  List<Seat> findByCinemaRoomId(Integer roomId);

  /**
   * Tìm tất cả ghế đang hoạt động trong một phòng chiếu cụ thể.
   *
   * @param roomId ID của phòng chiếu
   * @return danh sách các ghế đang hoạt động trong phòng chiếu
   */
  // List<Seat> findByCinemaRoomIdAndIsActiveTrue(Integer roomId);
  @Query("""
          SELECT s
          FROM Seat s
          JOIN FETCH s.seatType
          WHERE s.cinemaRoom.cinemaRoomId = :roomId
            AND s.isActive = true
          ORDER BY s.seatId ASC
      """)
  List<Seat> findActiveSeatsByRoomId(@Param("roomId") int roomId);

  /**
   * Tìm tất cả ghế đang hoạt động trong một phòng chiếu cụ thể, sắp xếp theo hàng
   * và cột.
   *
   * @param roomId ID của phòng chiếu
   * @return danh sách các ghế đang hoạt động trong phòng chiếu, sắp xếp theo hàng
   *         và cột
   */
  @Query("SELECT DISTINCT s.seatColumn FROM Seat s WHERE s.cinemaRoom.cinemaRoomId = :cinemaRoomId ORDER BY s.seatColumn")
  List<String> findDistinctSeatColumnsByRoomId(@Param("cinemaRoomId") Integer roomId);
}
