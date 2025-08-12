package com.example.projectwebmovie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComboDTO {
    private Integer comboId;
    private String comboName;
    private String description;
    private Integer price;
    private Integer discountPercentage;
    private Integer discountedPrice;
    private boolean active;
    private String imageUrl;
    private String comboStatus; // Lưu giá trị enum dưới dạng String

    public String getFormattedDiscountedPrice() {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(discountedPrice != null ? discountedPrice : 0);
    }

    public String getFormattedPrice() {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price != null ? price : 0);
    }
}