package com.example.projectwebmovie.controller;

import com.example.projectwebmovie.dto.CinemaRoomDTO;
import com.example.projectwebmovie.dto.MovieRoomCalenderDTO;
import com.example.projectwebmovie.dto.MovieScheduleForShowToAdminDTO;
import com.example.projectwebmovie.dto.SeatForCreateDTO;
import com.example.projectwebmovie.dto.SeatForCreateRequestDTO;
import com.example.projectwebmovie.service.CinemaRoomService;
import com.example.projectwebmovie.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class RoomController {
    @Autowired
    private SeatService seatService;
    @Autowired
    private CinemaRoomService cinemaRoomService;
    @GetMapping("/admin/room-management/room_detail")
    public String roomDetail(@RequestParam Integer cinemaRoomId, Model model) {
        model.addAttribute("cinemaRoom", cinemaRoomService.getById(cinemaRoomId));
        return "room/room_detail";
    }
    @GetMapping("/api/getSeatMap")
    @ResponseBody
    public Map<Integer, Map<String, SeatForCreateDTO>> getSeatMap(@RequestParam Integer cinemaRoomId){
        return seatService.getSeatMapByRoomId(cinemaRoomId);
    }
    @PostMapping("/api/saveSeatMap")
    @ResponseBody
    public String saveSeatMap( @RequestParam Integer cinemaRoomId,
                               @RequestBody List<SeatForCreateRequestDTO> seatList) {
        seatService.updateSeatMap(cinemaRoomId, seatList);
        return "success";
    }   
    @GetMapping("/api/room-list")
    @ResponseBody
    public List<String> getRoomList() {
        return cinemaRoomService.getAll().stream()
                .map(room -> room.getCinemaRoomName())
                .toList();
    }
    
}
