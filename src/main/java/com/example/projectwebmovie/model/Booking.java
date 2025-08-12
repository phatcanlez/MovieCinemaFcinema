package com.example.projectwebmovie.model;

import com.example.projectwebmovie.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "MOVIETHEATER_BOOKING")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @Column(name = "BOOKING_ID", length = 10)
    private String bookingId; // Mã đặt vé, khóa chính, tối đa 10 ký tự

    @Column(name = "ACCOUNT_ID", length = 10)
    private String accountId; // Mã tài khoản khách hàng, liên kết với MOVIETHEATER_ACCOUNT

    @Column(name = "MOVIE_ID", length = 10)
    private String movieId; // Mã phim, liên kết với MOVIETHEATER_MOVIE

    @Column(name = "SCHEDULE_ID")
    private Integer scheduleId; // Mã lịch chiếu, liên kết với MOVIETHEATER_SCHEDULE

    @Column(name = "SHOW_DATE_ID")
    private Integer showDateId; // Mã ngày chiếu, liên kết với MOVIETHEATER_SHOW_DATES

    @Column(name = "PROMOTION_ID")
    private String promotionId; // Mã khuyến mãi, liên kết với MOVIETHEATER_PROMOTION

    @Column(name = "BOOKING_DATE")
    private LocalDateTime bookingDate; // Ngày đặt vé (ví dụ: "2025-06-03 13:00")

    @Enumerated(EnumType.STRING)
    @Column(name = "BOOKING_STATUS", length = 20)
    private BookingStatus status; // Trạng thái đặt vé

    @Column(name = "TOTAL_PRICE")
    private Double totalPrice; // Tổng tiền (sau khi áp dụng điểm và khuyến mãi)

    @Column(name = "USED_POINTS")
    private Integer usedPoints; // Số điểm đã sử dụng để giảm giá

    @Column(name = "CANCEL_REASON", length = 255)
    private String cancelReason; // Lý do hủy đặt vé (nếu STATUS = 'CANCELLED')

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID", insertable = false, updatable = false)
    private Account account; // Tài khoản khách hàng, quan hệ nhiều-một

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MOVIE_ID", insertable = false, updatable = false)
    private Movie movie; // Phim liên quan, quan hệ nhiều-một

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_ID", insertable = false, updatable = false)
    private Schedule schedule; // Lịch chiếu liên quan, quan hệ nhiều-một

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SHOW_DATE_ID", insertable = false, updatable = false)
    private ShowDates showDates; // Ngày chiếu liên quan, quan hệ nhiều-một

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROMOTION_ID", insertable = false, updatable = false)
    private Promotion promotion; // Khuyến mãi áp dụng, quan hệ nhiều-một

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingSeat> bookingSeats; // Danh sách ghế được đặt, quan hệ một-nhiều

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments; // Danh sách thanh toán, quan hệ một-nhiều

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Invoice> invoices; // Danh sách hóa đơn, quan hệ một-nhiều

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingCombo> bookingCombos; // Danh sách combo được đặt, quan hệ một-nhiều

    @Override
    public String toString() {
        return "Booking{id='" + this.bookingId + "', movie=" + movie + ", account=" + account + "}";
    }
}