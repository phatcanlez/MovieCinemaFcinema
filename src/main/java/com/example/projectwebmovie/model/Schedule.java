package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "MOVIETHEATER_SCHEDULE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    @Id
    @Column(name = "SCHEDULE_ID")
    private Integer scheduleId; // Mã lịch chiếu, khóa chính

    @Column(name = "SCHEDULE_TIME", length = 255)
    private String scheduleTime; // Thời gian chiếu (ví dụ: "14:00")

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MovieSchedule> movieSchedules; // Danh sách lịch chiếu của phim, quan hệ một-nhiều

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScheduleSeat> scheduleSeats; // Danh sách ghế theo lịch chiếu, quan hệ một-nhiều (bổ sung)

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings; // Danh sách đặt vé cho lịch chiếu này, quan hệ một-nhiều (bổ sung)
}