package com.example.projectwebmovie.model;

import jakarta.persistence.*;

@Entity
@Table(name = "otp")
public class Otp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "expiry_time", nullable = false)
    private Long expiryTime; // Thời gian hết hạn (timestamp)

    // Getters và Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getExpiryTime() { return expiryTime; }
    public void setExpiryTime(Long expiryTime) { this.expiryTime = expiryTime; }
}