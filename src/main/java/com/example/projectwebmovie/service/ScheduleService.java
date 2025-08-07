package com.example.projectwebmovie.service;

import com.example.projectwebmovie.model.Movie;
import com.example.projectwebmovie.model.MovieSchedule;
import com.example.projectwebmovie.model.Schedule;
import com.example.projectwebmovie.repository.MovieRepository;
import com.example.projectwebmovie.repository.MovieScheduleRepository;
import com.example.projectwebmovie.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MovieScheduleRepository movieScheduleRepository;


    public List<MovieSchedule> getAllSchedulesByMovieId(String movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + movieId));

        System.out.println("List of schedules: " + movie.getMovieSchedules());

        return movie.getMovieSchedules();
    }

    public void createSchedule(Schedule schedule) {

        scheduleRepository.save(schedule);
    }



}
