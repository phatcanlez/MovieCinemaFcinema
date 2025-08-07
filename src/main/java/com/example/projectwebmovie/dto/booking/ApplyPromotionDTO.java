package com.example.projectwebmovie.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplyPromotionDTO {
    private String promotionCode;
    private Double originalAmount;
    private String accountId;
}
