package com.poly.controller;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.poly.model.User;
import com.poly.repository.UserRepository;
import com.poly.service.EmailService;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Hiển thị form nhập email
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "pages/forgot-password";
    }

    // Gửi mã OTP
    @PostMapping("/forgot-password")
    @Transactional
    public String sendOtp(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Email không tồn tại!");
            return "redirect:/forgot-password";
        }

        // Sinh mã OTP ngẫu nhiên 4 chữ số
        String otp = String.format("%04d", new Random().nextInt(10000));
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5)); // hết hạn sau 5 phút
        userRepository.save(user);

        // Gửi mail
        String subject = "Mã xác thực GameFi - Quên mật khẩu";
        String content = "Xin chào " + user.getUsername() + ",\n\n"
                + "Mã OTP của bạn là: " + otp + "\n"
                + "Mã có hiệu lực trong 5 phút.\n\n"
                + "Trân trọng,\nGameFi Team.";
        emailService.sendMail(email, subject, content);

        redirectAttributes.addFlashAttribute("success", "Đã gửi mã OTP đến email!");
        redirectAttributes.addFlashAttribute("email", email);
        return "redirect:/verify-otp?email=" + email;
    }

    // Form nhập OTP và mật khẩu mới
    @GetMapping("/verify-otp")
    public String showOtpForm(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "pages/verify-otp";
    }

    // Xác thực OTP
    @PostMapping("/verify-otp")
    @Transactional
    public String verifyOtp(@RequestParam("email") String email,
                            @RequestParam("otp") String otp,
                            @RequestParam("password") String password,
                            @RequestParam("confirm") String confirm,
                            RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Email không tồn tại!");
            return "redirect:/forgot-password";
        }

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp)) {
            redirectAttributes.addFlashAttribute("error", "Mã OTP không đúng!");
            return "redirect:/verify-otp?email=" + email;
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("error", "Mã OTP đã hết hạn!");
            return "redirect:/forgot-password";
        }

        if (!password.equals(confirm)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "redirect:/verify-otp?email=" + email;
        }

        // Đặt lại mật khẩu
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Đặt lại mật khẩu thành công! Hãy đăng nhập lại.");
        return "redirect:/login";
    }
}
