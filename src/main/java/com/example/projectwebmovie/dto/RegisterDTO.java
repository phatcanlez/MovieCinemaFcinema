package com.example.projectwebmovie.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDTO {

    @NotBlank(message = "Tên đăng nhập là bắt buộc")
    @Size(min = 3, max = 255, message = "Tên đăng nhập phải từ 3 đến 255 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    @Size(min = 6, max = 255, message = "Mật khẩu phải từ 6 đến 255 ký tự")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu là bắt buộc")
    @Size(min = 6, max = 255, message = "Xác nhận mật khẩu phải từ 6 đến 255 ký tự")
    private String confirmPassword;

    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String email;
}