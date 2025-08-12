package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.dto.CinemaRoomDTO;
import com.example.projectwebmovie.model.CinemaRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CinemaRoomRepository extends JpaRepository<CinemaRoom, Integer> {

    CinemaRoom findByCinemaRoomName(String name);

    @Query("""
            SELECT new com.example.projectwebmovie.dto.CinemaRoomDTO(
                cr.cinemaRoomId,
                cr.cinemaRoomName,
                COALESCE(COUNT(s.seatId), 0L),
                cr.isActive
            )
            FROM CinemaRoom cr
            LEFT JOIN cr.seats s
            WHERE s.isActive = true OR s.isActive IS NULL
            GROUP BY cr.cinemaRoomId, cr.cinemaRoomName, cr.isActive
            ORDER BY cr.cinemaRoomId
            """)
    Page<CinemaRoomDTO> getCinemaRoomsWithActiveSeatCount(Pageable pageable);

    @Query("""
            SELECT new com.example.projectwebmovie.dto.CinemaRoomDTO(
                cr.cinemaRoomId,
                cr.cinemaRoomName,
                COALESCE(COUNT(s.seatId), 0L),
                cr.isActive
            )
            FROM CinemaRoom cr
            LEFT JOIN cr.seats s
            WHERE s.isActive = true OR s.isActive IS NULL
            GROUP BY cr.cinemaRoomId, cr.cinemaRoomName, cr.isActive
            ORDER BY cr.cinemaRoomId
            """)
    List<CinemaRoomDTO> getCinemaRoomsWithActiveSeatCount();

    @Query("""
            SELECT new com.example.projectwebmovie.dto.CinemaRoomDTO(
                cr.cinemaRoomId,
                cr.cinemaRoomName,
                COALESCE(COUNT(s.seatId), 0L),
                cr.isActive
            )
            FROM CinemaRoom cr
            LEFT JOIN cr.seats s
            WHERE (
                LOWER(cr.cinemaRoomName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR CAST(cr.cinemaRoomId AS string) = :search
            )
            AND (s.isActive = true OR s.isActive IS NULL)
            GROUP BY cr.cinemaRoomId, cr.cinemaRoomName, cr.isActive
            ORDER BY cr.cinemaRoomId
            """)
    Page<CinemaRoomDTO> findByCinemaRoomNameContainingOrCinemaRoomId(
            @Param("search") String search,
            Pageable pageable);

    List<CinemaRoom> findByIsActiveTrueOrderByCinemaRoomIdAsc();

    List<CinemaRoom> findByIsActiveFalseOrderByCinemaRoomIdAsc();

}