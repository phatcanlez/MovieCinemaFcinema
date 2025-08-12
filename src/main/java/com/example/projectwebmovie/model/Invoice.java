package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "MOVIETHEATER_INVOICE")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Invoice {
    @Id
    @Column(name = "INVOICE_ID", length = 10)
    private String invoiceId; // Mã hóa đơn, khóa chính, tối đa 10 ký tự

    @Column(name = "BOOKING_ID", length = 10)
    private String bookingId; // Mã đặt vé, liên kết với MOVIETHEATER_BOOKING, tối đa 10 ký tự (bổ sung)

    @Column(name = "ACCOUNT_ID", length = 10) // Giảm length từ 255 xuống 10 để khớp với ACCOUNT_ID
    private String accountId; // Mã tài khoản khách hàng, liên kết với MOVIETHEATER_ACCOUNT

    @Column(name = "ADD_SCORE")
    private Integer addScore; // Số điểm được cộng từ đặt vé này (1k = 1 điểm)

    @Column(name = "BOOKING_DATE")
    private LocalDateTime bookingDate; // Ngày tạo hóa đơn

    @Column(name = "STATUS")
    private Integer status; // Trạng thái hóa đơn (ví dụ: 1 = Hoàn thành, 0 = Đã hủy)

    @Column(name = "TOTAL_MONEY")
    private Integer totalMoney; // Tổng tiền (bằng TOTAL_PRICE trong MOVIETHEATER_BOOKING)

    @Column(name = "USE_SCORE")
    private Integer useScore; // Số điểm đã sử dụng (bằng USED_POINTS trong MOVIETHEATER_BOOKING)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID", insertable = false, updatable = false)
    private Account account; // Tài khoản khách hàng, quan hệ nhiều-một

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOKING_ID", insertable = false, updatable = false)
    private Booking booking; // Đặt vé liên quan đến hóa đơn, quan hệ nhiều-một (bổ sung)
}