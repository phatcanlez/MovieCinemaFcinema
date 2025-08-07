package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.MovieType;
import com.example.projectwebmovie.model.MovieTypeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieTypeRepository extends JpaRepository<MovieType, MovieTypeId> {
    List<MovieType> findByMovie_MovieId(String movieId);
    void deleteByMovie_MovieId(String movieId);
    @Query("SELECT t.typeName FROM MovieType mt JOIN mt.type t WHERE mt.movie.movieId = :movieId")
    List<String> findTypeNamesByMovieId(@Param("movieId") String movieId);
}

