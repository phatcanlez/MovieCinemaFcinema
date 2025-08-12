package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MOVIETHEATER_BOOKING_COMBO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCombo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BOOKING_COMBO_ID")
    private Integer bookingComboId; // Mã đặt combo, khóa chính, tự tăng

    @Column(name = "BOOKING_ID", length = 10, nullable = false)
    private String bookingId; // Mã đặt vé, liên kết với MOVIETHEATER_BOOKING

    @Column(name = "COMBO_ID", nullable = false)
    private Integer comboId; // Mã combo, liên kết với MOVIETHEATER_COMBO

    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity; // Số lượng combo được đặt

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOKING_ID", insertable = false, updatable = false)
    private Booking booking; // Đặt vé liên quan, quan hệ nhiều-một

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMBO_ID", insertable = false, updatable = false)
    private Combo combo; // Combo liên quan, quan hệ nhiều-một

    @Override
    public String toString() {
        return "BookingCombo{bookingComboId=" + bookingComboId + ", bookingId='" + bookingId + "', comboId=" + comboId + ", quantity=" + quantity + "}";
    }
}