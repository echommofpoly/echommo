package com.poly.service;

import com.poly.dto.AuthResponse;
import com.poly.dto.LoginRequest;
import com.poly.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse authenticate(LoginRequest request);
}