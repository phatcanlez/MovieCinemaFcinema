package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.List;

@Entity
@Table(name = "MOVIETHEATER_ROLE")
@NoArgsConstructor
@Getter
@Setter
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ROLE_ID")
    private Integer roleId; // Mã vai trò, tự tăng

    @Column(name = "ROLE_NAME", length = 255)
    private String roleName; // Tên vai trò

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    private List<Account> accounts; // Danh sách tài khoản, giữ LAZY với BatchSize
}