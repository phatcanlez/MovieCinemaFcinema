package com.example.projectwebmovie.controller;

import com.example.projectwebmovie.dto.ScheduleDTO;
import com.example.projectwebmovie.model.Movie;

import com.example.projectwebmovie.repository.MovieRepository;
import com.example.projectwebmovie.service.MovieScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;;

@Controller
@RequestMapping("/admin/movie-schedule")
public class MovieScheduleController {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieScheduleService movieScheduleService;

    public MovieScheduleController(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @GetMapping()
    @ResponseBody
    public List<ScheduleDTO> getScheduleData(
            @RequestParam("movieId") String movieId,
            @RequestParam("roomId") Integer roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate) {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim với ID: " + movieId));
        if (movie == null)
            return List.of();
        // 2. Trả danh sách giờ khả dụng, có đánh dấu selected
        return movieScheduleService.getScheduleOptionsForMovieAndRoom(movie, roomId, showDate);
    }

    /**
     * Lưu lịch chiếu phim vào nhiều phòng chiếu
     * Dữ liệu từ form gửi lên là danh sách roomId_scheduleId (VD: 1_5, 1_6,
     * 2_3,...)
     */
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveScheduleAjax(@RequestBody Map<String, Object> payload) {
        String movieId = (String) payload.get("movieId");
        String showDateStr = (String) payload.get("showDate");
        List<String> roomScheduleIds = (List<String>) payload.get("scheduleRoomMap");

        LocalDate showDate = LocalDate.parse(showDateStr);

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phim với ID: " + movieId));

        Map<Integer, List<Integer>> roomScheduleMap = new HashMap<>();
        for (String pair : roomScheduleIds) {
            String[] parts = pair.split("_");
            int roomId = Integer.parseInt(parts[0]);
            int scheduleId = Integer.parseInt(parts[1]);
            roomScheduleMap.computeIfAbsent(roomId, k -> new ArrayList<>()).add(scheduleId);
        }

        List<String> failedRooms = new ArrayList<>();

        for (Map.Entry<Integer, List<Integer>> entry : roomScheduleMap.entrySet()) {
            try {
                movieScheduleService.saveSchedulesForMovie(movie, entry.getValue(), entry.getKey(), showDate);
            } catch (IllegalArgumentException e) {
                failedRooms.add("Phòng " + entry.getKey() + ": " + e.getMessage());
            }
        }

        if (!failedRooms.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "fail",
                    "errors", failedRooms));
        }

        return ResponseEntity.ok(Map.of("status", "ok"));
    }

}
