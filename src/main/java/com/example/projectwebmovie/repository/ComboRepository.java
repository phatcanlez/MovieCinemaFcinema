package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComboRepository extends JpaRepository<Combo, Integer> {
    boolean existsByComboName(String comboName);

    // Lấy combo dựa trên comboId
    @Query("SELECT c FROM Combo c WHERE c.comboId = :comboId")
    Optional<Combo> findByComboId(@Param("comboId") Integer comboId);

    // Kiểm tra combo có active không
    boolean existsByComboIdAndActiveIsTrue(Integer comboId);

}