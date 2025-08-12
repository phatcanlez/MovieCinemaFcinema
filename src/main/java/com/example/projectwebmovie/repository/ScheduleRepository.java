package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
    List<Schedule> findAllByOrderByScheduleTimeAsc();
}