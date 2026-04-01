package com.daily.note.save.security;

import java.time.LocalDateTime;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.daily.note.save.dto.LoginRequestDto;
import com.daily.note.save.dto.LoginResponseDto;
import com.daily.note.save.dto.ResetPasswordDto;
import com.daily.note.save.dto.SignupRequestDto;
import com.daily.note.save.dto.SignupResposeDto;
import com.daily.note.save.dto.VerifyOtpDto;
import com.daily.note.save.entity.User;
import com.daily.note.save.repository.UserRepository;
import com.daily.note.save.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ModelMapper modelMapper;
    private final JwtUtil jwtUtil;

    private String generateOtp() {
        return String.valueOf(
                new java.security.SecureRandom().nextInt(900000) + 100000
        );
    }

    public SignupResposeDto signup(SignupRequestDto signupRequestDto) {
        User existingUser = userRepository.findByEmail(signupRequestDto.getEmail()).orElse(null);
        if (existingUser != null) {
            throw new IllegalArgumentException("User already exists with email: " + signupRequestDto.getEmail());
        }
        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());
        String otp = generateOtp();
        User user = new User();
        user.setName(signupRequestDto.getName());
        user.setEmail(signupRequestDto.getEmail());
        user.setPassword(encodedPassword);
        //OTP fields
        user.setOtp(otp);
        user.setVerified(false);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(5));
        User newUser = userRepository.save(user);
        //send OTP on email
        emailService.sendOtp(newUser.getEmail(), otp);
        return modelMapper.map(newUser, SignupResposeDto.class);
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + loginRequestDto.getEmail()));
        if (!user.isVerified()) {
            throw new IllegalArgumentException("Please verify your email with OTP first");
        }
        if (!passwordEncoder.matches(loginRequestDto.getPassword(),user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }
        String token = jwtUtil.generateToken(user.getEmail());
        return new LoginResponseDto(token, user.getName(), user.getEmail());
    }

    public String verifyOtp(VerifyOtpDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getOtp() == null || user.getOtpExpiry() == null) {
            throw new RuntimeException("OTP not available");
        }
        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }
        if (!user.getOtp().equals(dto.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }
        user.setVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        return "Email verified successfully";
    }

    public String resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isVerified()) {
            throw new RuntimeException("Email already verified");
        }

        String otp = generateOtp();

        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        emailService.sendOtp(user.getEmail(), otp);

        return "OTP resent successfully";
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = generateOtp();

        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        emailService.sendOtp(email, otp);

        return "Password reset OTP sent";
    }

    public String resetPassword(ResetPasswordDto dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtp() == null || user.getOtpExpiry() == null) {
            throw new RuntimeException("OTP not available");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!user.getOtp().equals(dto.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setOtp(null);
        user.setOtpExpiry(null);

        userRepository.save(user);

        return "Password reset successful";
    }

}
