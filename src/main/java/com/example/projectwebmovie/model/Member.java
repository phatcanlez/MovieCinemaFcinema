package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "MOVIETHEATER_MEMBER")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Member {
    @Id
    @Column(name = "MEMBER_ID", length = 10)
    private String memberId; // Mã thành viên, khóa chính

    @Column(name = "SCORE", nullable = false)
    private Integer score; // Điểm tích lũy

    @Column(name = "ACCOUNT_ID", length = 10)
    private String accountId; // Mã tài khoản

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ACCOUNT_ID", referencedColumnName = "ACCOUNT_ID", insertable = false, updatable = false)
    private Account account; // Quan hệ một-một, tải ngay
}