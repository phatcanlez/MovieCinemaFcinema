package com.example.projectwebmovie.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.projectwebmovie.dto.MovieRoomCalenderDTO;
import com.example.projectwebmovie.repository.MovieScheduleRepository;
import com.example.projectwebmovie.service.MovieScheduleService;

@Controller
public class ScheduleController {

    @Autowired
    private MovieScheduleService movieScheduleService;

    @GetMapping("/api/schedule/movie-room-calender")
    @ResponseBody
    public ResponseEntity<List<MovieRoomCalenderDTO>> getMovieRoomCalender(
            @RequestParam("showDate") LocalDate showDate) {
        List<MovieRoomCalenderDTO> calender = movieScheduleService.getMovieRoomCalenderByShowDate(showDate);
        return ResponseEntity.ok(calender);
    }

    @GetMapping("/admin/schedule")
    public String getSchedule() {
        return "admin/schedule-movie";
    }

}
