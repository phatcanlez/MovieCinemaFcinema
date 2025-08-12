package com.example.projectwebmovie.mapper;

import com.example.projectwebmovie.dto.CinemaRoomDTO;
import com.example.projectwebmovie.model.CinemaRoom;

public class CinemaRoomMapper {


    public static CinemaRoomDTO toDTO(CinemaRoom cinemaRoom) {
        if (cinemaRoom == null) {
            return null;
        }

        CinemaRoomDTO dto = new CinemaRoomDTO();
        dto.setCinemaRoomId(cinemaRoom.getCinemaRoomId());
        dto.setCinemaRoomName(cinemaRoom.getCinemaRoomName());

        return dto;
    }
}
