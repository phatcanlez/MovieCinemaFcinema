package com.example.projectwebmovie.mapper;

import com.example.projectwebmovie.dto.SeatForCreateDTO;
import com.example.projectwebmovie.dto.SeatForCreateRequestDTO;
import com.example.projectwebmovie.dto.SeatScheduleDTO;
import com.example.projectwebmovie.model.ScheduleSeat;
import com.example.projectwebmovie.model.Seat;
import com.example.projectwebmovie.model.SeatType;
import com.example.projectwebmovie.repository.SeatTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class SeatMapper {
    @Autowired
    private SeatTypeRepository seatTypeRepository;

    public static SeatForCreateDTO toDTO(Seat seat) {
        if (seat == null)
            return null;

        SeatForCreateDTO dto = new SeatForCreateDTO();
        dto.setSeatId(seat.getSeatId());
        dto.setSeatColumn(seat.getSeatColumn());
        dto.setSeatRow(seat.getSeatRow());
        dto.setActive(seat.isActive());
        if (seat.getSeatType() != null) {
            dto.setSeatType(seat.getSeatType().getName());
        }
        return dto;
    }

    public static Seat toEntity(SeatForCreateDTO dto) {
        if (dto == null)
            return null;

        Seat seat = new Seat();
        seat.setSeatId(dto.getSeatId());
        seat.setSeatColumn(dto.getSeatColumn());
        seat.setSeatRow(dto.getSeatRow());
        seat.setActive(dto.isActive());
        return seat;
    }

    public static SeatScheduleDTO toSeatScheduleDTO(ScheduleSeat seat) {
        if (seat == null)
            return null;

        SeatScheduleDTO dto = new SeatScheduleDTO();
        dto.setSeatId(seat.getSeatId());
        dto.setSeatColumn(seat.getSeatColumn());
        dto.setSeatRow(seat.getSeatRow());
        dto.setSeatType(seat.getSeatType().getName());
        dto.setSeatStatus(seat.getSeatStatus() != null ? seat.getSeatStatus() : 0);
        dto.setSeatPrice(seat.getSeatPrice() != null ? seat.getSeatPrice() : 0.0);
        return dto;
    }


}
