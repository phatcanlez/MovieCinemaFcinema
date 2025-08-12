package com.example.projectwebmovie.controller;

import com.example.projectwebmovie.dto.CinemaRoomDTO;
import com.example.projectwebmovie.dto.MovieRoomCalenderDTO;
import com.example.projectwebmovie.dto.MovieScheduleForShowToAdminDTO;
import com.example.projectwebmovie.dto.SeatForCreateDTO;
import com.example.projectwebmovie.dto.SeatForCreateRequestDTO;
import com.example.projectwebmovie.service.CinemaRoomService;
import com.example.projectwebmovie.service.MovieScheduleService;
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
    @Autowired
    private MovieScheduleService movieScheduleService;

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
        /**
     * API đổi tên phòng chiếu, chỉ cho phép nếu phòng chưa có lịch chiếu
     * @param cinemaRoomId id phòng cần đổi
     * @param newRoomName tên phòng mới
     * @return success hoặc error message
     */
    @PostMapping("/api/rename-room")
    @ResponseBody
    public String renameRoom(@RequestParam Integer cinemaRoomId, @RequestParam String newRoomName) {
        // Inject MovieScheduleService nếu cần, hoặc autowire nếu đã có
        // (Giả sử đã autowire MovieScheduleService)
        // Nếu chưa, cần thêm @Autowired MovieScheduleService movieScheduleService;
        //
        // Để tránh lỗi, kiểm tra và autowire nếu cần:
        //
        // @Autowired
        // private MovieScheduleService movieScheduleService;

        // Đổi tên phòng nếu chưa có lịch chiếu
        int updated = cinemaRoomService.updateRoomInfoIfNoSchedule(cinemaRoomId, newRoomName);
        if(updated == 1) {
            return "success";
        } else if (updated == -1) {
            return "error: Room already has schedules, cannot rename.";
        } else if (updated == -2) {
            return "error: Room name already exists, cannot rename.";
        }else{ 
            return "error: Unknown error occurred.";
        }
    }
    @PostMapping("/api/can-rename-room")
    @ResponseBody
    public String canRenameRoom(@RequestParam Integer cinemaRoomId) {
        // Inject MovieScheduleService nếu cần, hoặc autowire nếu đã có
        // (Giả sử đã autowire MovieScheduleService)
        // Nếu chưa, cần thêm @Autowired MovieScheduleService movieScheduleService;
        //
        // Để tránh lỗi, kiểm tra và autowire nếu cần:
        //
        // @Autowired
        // private MovieScheduleService movieScheduleService;

        // Đổi tên phòng nếu chưa có lịch chiếu
        return movieScheduleService.existsMovieScheduleByRoomId(cinemaRoomId) ? "false" : "true";
    }
}
