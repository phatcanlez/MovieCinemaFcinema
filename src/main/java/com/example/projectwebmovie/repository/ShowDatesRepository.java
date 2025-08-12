package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.ShowDates;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.time.LocalDate;

public interface ShowDatesRepository extends JpaRepository<ShowDates, Integer> {
    Optional<ShowDates> findByShowDate(LocalDate showDate);
}