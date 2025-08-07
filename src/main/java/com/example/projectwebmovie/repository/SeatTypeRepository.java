package com.example.projectwebmovie.repository;


import com.example.projectwebmovie.model.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatTypeRepository extends JpaRepository<SeatType, Integer> {
    //String getNameById(Integer id);
    @Query("SELECT st.id FROM SeatType st WHERE st.name = :name")
    Integer getIdByName(@Param("name") String name);

    Optional<SeatType> findByName(String typeName);
}
