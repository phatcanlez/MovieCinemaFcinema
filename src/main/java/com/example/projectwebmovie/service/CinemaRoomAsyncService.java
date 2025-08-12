package com.example.projectwebmovie.service;

import com.example.projectwebmovie.dto.SeatForCreateDTO;
import com.example.projectwebmovie.service.CinemaRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CinemaRoomAsyncService {

    @Autowired
    private CinemaRoomService cinemaRoomService;

    @Async
    public void addNewRoomAsync(String nameRoom, List<SeatForCreateDTO> seatList) {
        cinemaRoomService.addNewRoom(nameRoom, seatList);
    }
}