package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.dto.CinemaRoomDTO;
import com.example.projectwebmovie.model.CinemaRoom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CinemaRoomRepository extends JpaRepository<CinemaRoom,Integer> {
    CinemaRoom findByCinemaRoomName(String name);
    @Query(value = """
    SELECT new com.example.projectwebmovie.dto.CinemaRoomDTO(
        cr.cinemaRoomId,
        cr.cinemaRoomName,
        COUNT(s.seatId)
    )
    FROM CinemaRoom cr
    LEFT JOIN Seat s ON cr.cinemaRoomId = s.cinemaRoomId AND s.isActive = true
    GROUP BY cr.cinemaRoomId, cr.cinemaRoomName
    ORDER BY cr.cinemaRoomId
    """,
            countQuery = """
    SELECT COUNT(cr.cinemaRoomId)
    FROM CinemaRoom cr
    """)
    Page<CinemaRoomDTO> getCinemaRoomsWithActiveSeatCount(Pageable pageable);

    @Query("""
    SELECT new com.example.projectwebmovie.dto.CinemaRoomDTO(
        cr.cinemaRoomId,
        cr.cinemaRoomName,
        COUNT(s.seatId)
    )
    FROM CinemaRoom cr
    LEFT JOIN Seat s ON cr.cinemaRoomId = s.cinemaRoomId AND s.isActive = true
    GROUP BY cr.cinemaRoomId, cr.cinemaRoomName
    ORDER BY cr.cinemaRoomId
""")
    List<CinemaRoomDTO> getCinemaRoomsWithActiveSeatCount();

    /**
     * Tìm kiếm phòng chiếu theo tên LIKE hoặc id = search (nếu search là số)
     */
    @Query(value = """
        SELECT new com.example.projectwebmovie.dto.CinemaRoomDTO(
            cr.cinemaRoomId,
            cr.cinemaRoomName,
            COUNT(s.seatId)
        )
        FROM CinemaRoom cr
        LEFT JOIN Seat s ON cr.cinemaRoomId = s.cinemaRoomId AND s.isActive = true
        WHERE (
            LOWER(cr.cinemaRoomName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR (CAST(:search AS string) IS NOT NULL AND FUNCTION('str', cr.cinemaRoomId) = :search)
        )
        GROUP BY cr.cinemaRoomId, cr.cinemaRoomName
        ORDER BY cr.cinemaRoomId
    """,
    countQuery = """
        SELECT COUNT(cr.cinemaRoomId)
        FROM CinemaRoom cr
        WHERE (
            LOWER(cr.cinemaRoomName) LIKE LOWER(CONCAT('%', :search, '%'))
            OR (CAST(:search AS string) IS NOT NULL AND FUNCTION('str', cr.cinemaRoomId) = :search)
        )
    """)
    Page<CinemaRoomDTO> findByCinemaRoomNameContainingOrCinemaRoomId(@org.springframework.data.repository.query.Param("search") String search, Pageable pageable);

}
