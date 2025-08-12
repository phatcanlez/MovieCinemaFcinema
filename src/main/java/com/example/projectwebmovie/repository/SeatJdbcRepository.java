package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.Seat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.example.projectwebmovie.model.ScheduleSeat;

import java.math.BigDecimal;
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
                });
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
                });
    }

    public void insertSchedulesSeatsBatch(List<ScheduleSeat> scheduleSeats) {
        String sql = "INSERT INTO movietheater_schedule_seat (\r\n" + //
                "                schedule_seat_id, movie_id, schedule_id, show_date_id, cinema_room_id,\r\n" + //
                "                seat_id, seat_row, seat_column, seat_type_id, seat_status, seat_price\r\n" + //
                "            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, scheduleSeats, scheduleSeats.size(),
                (ps, ss) -> {
                    ps.setString(1, ss.getScheduleSeatId());
                    ps.setString(2, ss.getMovieId());
                    ps.setLong(3, ss.getScheduleId());
                    ps.setLong(4, ss.getShowDateId());
                    ps.setInt(5, ss.getCinemaRoomId());
                    ps.setLong(6, ss.getSeatId());
                    ps.setInt(7, ss.getSeatRow());
                    ps.setString(8, ss.getSeatColumn());
                    ps.setInt(9, ss.getSeatType().getId());
                    ps.setInt(10, ss.getSeatStatus());
                    ps.setBigDecimal(11, BigDecimal.valueOf(ss.getSeatPrice()));
                });

    }
}
