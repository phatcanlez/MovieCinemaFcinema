package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.List;

@Entity
@Table(name = "MOVIETHEATER_EMPLOYEE")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Employee {
    @Id
    @Column(name = "EMPLOYEE_ID", length = 10)
    private String employeeId; // Mã nhân viên, khóa chính

    @Column(name = "ACCOUNT_ID", length = 10)
    private String accountId; // Mã tài khoản

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ACCOUNT_ID", referencedColumnName = "ACCOUNT_ID", insertable = false, updatable = false)
    private Account account; // Quan hệ một-một, tải ngay

}