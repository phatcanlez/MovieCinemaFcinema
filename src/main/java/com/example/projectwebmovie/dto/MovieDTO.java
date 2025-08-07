package com.example.projectwebmovie.dto;

import lombok.Data;

import java.util.List;

import org.springframework.format.annotation.NumberFormat;

@Data
public class MovieDTO {
    private String movieId;
    private Integer duration;
    private String fromDate; // Ngày bắt đầu chiếu (ví dụ: "2025-06-01")
    private String toDate;
    private String actor;
    private String director;
    private String content;
    private String version; // Phiên bản phim (ví dụ: "2D", "3D", "IMAX")
    private String movieNameEnglish; // Tên phim tiếng Anh
    private String movieNameVn; // Tên phim tiếng Việt, không được để trống
    private Integer rating; // Điểm đánh giá phim (ví dụ: 8 cho 8/10) (bổ sung)
    private String smallImage; // Đường dẫn hình ảnh nhỏ của phim
    private String largeImage; // Đường dẫn hình ảnh nhỏ của phim
    private String status; // Trạng thái phim (UPCOMING, SHOWING, ENDED) (bổ sung)
    private String trailerId;
    private Double price;
    private String movieProductionCompany;
    private List<String> types; // Danh sách các loại phim (ví dụ: "Action", "Comedy", "Drama") (bổ sung)
}
