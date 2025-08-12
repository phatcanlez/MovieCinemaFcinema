package com.example.projectwebmovie.model;

import com.example.projectwebmovie.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "MOVIETHEATER_PAYMENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @Column(name = "PAYMENT_ID", length = 10)
    private String paymentId; // Mã thanh toán, khóa chính, tối đa 10 ký tự

    @Column(name = "BOOKING_ID", length = 10)
    private String bookingId; // Mã đặt vé, liên kết với MOVIETHEATER_BOOKING

    @Column(name = "AMOUNT")
    private Double amount; // Số tiền thanh toán (bằng TOTAL_PRICE sau khi áp dụng điểm/khuyến mãi)

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_METHOD", length = 50)
    private PaymentMethod paymentMethod; // Phương thức thanh toán (CASH, MOMO)

    @Column(name = "PAYMENT_DATE")
    private LocalDateTime paymentDate; // Ngày thanh toán (ví dụ: "2025-06-03 13:00")

    @Column(name = "STATUS", length = 20)
    private String status; // Trạng thái thanh toán (PENDING, SUCCESS, FAILED)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOKING_ID", insertable = false, updatable = false)
    private Booking booking; // Đặt vé liên quan, quan hệ nhiều-một
}