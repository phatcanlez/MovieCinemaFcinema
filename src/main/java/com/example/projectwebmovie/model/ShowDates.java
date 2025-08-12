package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "MOVIETHEATER_SHOW_DATES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowDates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SHOW_DATE_ID")
    private Integer showDateId; // Mã ngày chiếu, khóa chính

    @Column(name = "SHOW_DATE")
    private LocalDate showDate; // Ngày chiếu (ví dụ: "2025-06-03")

    @Column(name = "DATE_NAME", length = 255)
    private String dateName; // Tên ngày (ví dụ: "Thứ Ba")

    @OneToMany(mappedBy = "showDates", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MovieDate> movieDates; // Danh sách ngày chiếu của phim, quan hệ một-nhiều

    @OneToMany(mappedBy = "showDates", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings; // Danh sách đặt vé cho ngày chiếu này, quan hệ một-nhiều (bổ sung)
}