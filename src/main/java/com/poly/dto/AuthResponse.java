package com.poly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    // Trong một ứng dụng thực tế, đây sẽ là nơi chứa JWT token
    private String message;
    private Object data; // Có thể là thông tin người dùng hoặc token
}