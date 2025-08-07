package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "MOVIETHEATER_ACCOUNT")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Account {
    @Id
    @Column(name = "ACCOUNT_ID", length = 10)
    private String accountId; // Mã tài khoản, khóa chính

    @Column(name = "ADDRESS", length = 255)
    private String address; // Địa chỉ

    @Column(name = "DATE_OF_BIRTH")
    private LocalDate dateOfBirth; // Ngày sinh

    @Column(name = "EMAIL", length = 255)
    private String email; // Email

    @Column(name = "FULL_NAME", length = 255)
    private String fullName; // Họ và tên

    @Column(name = "GENDER", length = 255)
    private String gender; // Giới tính

    @Column(name = "IDENTITY_CARD", length = 255)
    private String identityCard; // CMND/CCCD

    @Column(name = "IMAGE", length = 255)
    private String image; // Hình ảnh đại diện

    @Column(name = "PASSWORD", length = 255)
    private String password; // Mật khẩu

    @Column(name = "PHONE_NUMBER", length = 255)
    private String phoneNumber; // Số điện thoại

    @Column(name = "REGISTER_DATE")
    private LocalDate registerDate; // Ngày đăng ký

    @Column(name = "ROLE_ID")
    private Integer roleId; // Mã vai trò

    @Column(name = "STATUS")
    private Integer status; // Trạng thái (1 = Hoạt động, 0 = Khóa)

    @Column(name = "USERNAME", length = 255)
    private String username; // Tên đăng nhập

    @Column(name = "POINTS", nullable = true)
    private Integer points = 100; // Điểm mặc định khi tạo tài khoản

    @Column(name = "PROFILE_COMPLETED", nullable = false)
    private Boolean profileCompleted = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ROLE_ID", referencedColumnName = "ROLE_ID", insertable = false, updatable = false)
    private Role role; // Quan hệ nhiều-một, tải ngay

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<Invoice> invoices; // Danh sách hóa đơn, giữ LAZY với BatchSize

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<Booking> bookings; // Danh sách đặt vé, giữ LAZY với BatchSize

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Member member; // Quan hệ một-một, tải ngay

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Employee employee; // Quan hệ một-một, tải ngay
}