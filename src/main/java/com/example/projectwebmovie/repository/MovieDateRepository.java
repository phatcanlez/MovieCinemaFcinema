package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.MovieDate;
import com.example.projectwebmovie.model.Movie;
import com.example.projectwebmovie.model.ShowDates;
import com.example.projectwebmovie.model.MovieDateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieDateRepository extends JpaRepository<MovieDate, MovieDateId> {
    Optional<MovieDate> findByMovieAndShowDates(Movie movie, ShowDates showDates);
}
