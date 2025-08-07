package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "MOVIETHEATER_PROMOTION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {
    @Id
    @Column(name = "PROMOTION_ID")
    private String promotionId; // Mã khuyến mãi, khóa chính

    @Column(name = "DETAIL", length = 255)
    private String detail; // Chi tiết khuyến mãi, tối đa 255 ký tự

    @Column(name = "DISCOUNT_LEVEL")
    private Double discountLevel; // Mức giảm giá (ví dụ: 20 cho 20%)

    @Column(name = "DISCOUNT_AMOUNT")
    private Double discountAmount; // Giảm trực tiếp số tiền, đơn vị VNĐ

    @Column(name = "MAX_DISCOUNT_AMOUNT")
    private Double maxAmountForPercentDiscount; // Số tiền giảm tối đa nếu là giảm %

    @Column(name = "USAGE_LIMIT")
    private Integer usageLimit; // Số lượt được dùng tối đa

    @Column(name = "END_TIME")
    private LocalDateTime endTime; // Thời gian kết thúc khuyến mãi

    @Column(name = "IMAGE", length = 255)
    private String image; // Đường dẫn hình ảnh khuyến mãi, tối đa 255 ký tự

    @Column(name = "START_TIME")
    private LocalDateTime startTime; // Thời gian bắt đầu khuyến mãi

    @Column(name = "TITLE", length = 255)
    private String title; // Tiêu đề khuyến mãi, tối đa 255 ký tự

    @Column(name = "PROMOTION_CODE", length = 50, unique = true)
    private String promotionCode; // Mã khuyến mãi duy nhất, tối đa 50 ký tự

    @Column(name = "IS_ACTIVE")
    private Boolean isActive = true; // Trạng thái hoạt động của khuyến mãi, mặc định là true

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings; // Danh sách đặt vé áp dụng khuyến mãi, quan hệ một-nhiều (bổ sung)
}