package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.Seat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SeatJdbcRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insertSeatsBatch(List<Seat> seatList) {
        String sql = "INSERT INTO movietheater_seat (cinema_room_id, is_active, seat_column, seat_row, seat_type_id) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, seatList, seatList.size(),
                (ps, seat) -> {
                    ps.setInt(1, seat.getCinemaRoomId());
                    ps.setBoolean(2, seat.isActive());
                    ps.setString(3, seat.getSeatColumn());
                    ps.setInt(4, seat.getSeatRow());
                    ps.setInt(5, seat.getSeatTypeId());
                }
        );
    }
    public void updateSeatsBatch(List<Seat> seatList) {
        String sql = "UPDATE movietheater_seat SET is_active = ?, seat_column = ?, seat_row = ?, seat_type_id = ?, cinema_room_id = ? WHERE seat_id = ?";

        jdbcTemplate.batchUpdate(sql, seatList, seatList.size(),
                (ps, seat) -> {
                    ps.setBoolean(1, seat.isActive());
                    ps.setString(2, seat.getSeatColumn());
                    ps.setInt(3, seat.getSeatRow());
                    ps.setInt(4, seat.getSeatTypeId());
                    ps.setInt(5, seat.getCinemaRoomId());
                    ps.setInt(6, seat.getSeatId()); // WHERE id = ?
                }
        );
    }

}
