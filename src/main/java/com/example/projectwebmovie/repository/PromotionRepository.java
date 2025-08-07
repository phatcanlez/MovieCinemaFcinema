package com.example.projectwebmovie.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.projectwebmovie.model.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, String> {
    @Query("SELECT p FROM Promotion p WHERE CURRENT_TIMESTAMP BETWEEN p.startTime AND p.endTime ORDER BY p.promotionId DESC")
    List<Promotion> findCurrentActivePromotions();

    Promotion findTopByOrderByPromotionIdDesc();

    Promotion findByPromotionCode(String PromotionCode);
}
