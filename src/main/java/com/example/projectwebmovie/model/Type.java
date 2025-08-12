package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "MOVIETHEATER_TYPE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Type {
    @Id
    @Column(name = "TYPE_ID")
    private Integer typeId; // Mã thể loại, khóa chính

    @Column(name = "TYPE_NAME", length = 255)
    private String typeName; // Tên thể loại (ví dụ: "Hành động", "Hài")

    @OneToMany(mappedBy = "type", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<MovieType> movieTypes; // Danh sách thể loại của phim, quan hệ một-nhiều
}