package com.example.projectwebmovie.mapper;

import com.example.projectwebmovie.dto.promotion.RequestPromotionDTO;
import com.example.projectwebmovie.dto.promotion.PromotionDTO;
import com.example.projectwebmovie.model.Promotion;

public class PromotionMapper {
    public static PromotionDTO toDTO(Promotion promotion) {
        if (promotion == null) {
            return null;
        }
        PromotionDTO dto = new PromotionDTO();

        dto.setPromotionId(promotion.getPromotionId());
        dto.setDetail(promotion.getDetail());
        dto.setDiscountLevel(promotion.getDiscountLevel());
        dto.setDiscountAmount(promotion.getDiscountAmount());
        dto.setMaxAmountForPercentDiscount(promotion.getMaxAmountForPercentDiscount());
        dto.setUsageLimit(promotion.getUsageLimit());
        dto.setEndTime(promotion.getEndTime());
        dto.setPromotionImage(promotion.getImage());
        dto.setStartTime(promotion.getStartTime());
        dto.setTitle(promotion.getTitle());
        dto.setPromotionCode(promotion.getPromotionCode());
        dto.setIsActive(promotion.getIsActive());
        return dto;
    }

    public static Promotion toPromotion(RequestPromotionDTO dto) {
        if (dto == null) {
            return null;
        }
        Promotion promotion = new Promotion();
        promotion.setDetail(dto.getDetail());
        promotion.setDiscountLevel(dto.getDiscountLevel());
        promotion.setDiscountAmount(dto.getDiscountAmount());
        promotion.setMaxAmountForPercentDiscount(dto.getMaxAmountForPercentDiscount());
        promotion.setUsageLimit(dto.getUsageLimit());
        promotion.setEndTime(dto.getEndTime());
        promotion.setStartTime(dto.getStartTime());
        promotion.setTitle(dto.getTitle());
        promotion.setPromotionCode(dto.getPromotionCode().toUpperCase());
        promotion.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : false);
        return promotion;
    }

}
