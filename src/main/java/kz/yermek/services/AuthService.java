package kz.yermek.services;

import jakarta.transaction.Transactional;
import kz.yermek.dto.JwtRefreshTokenDto;
import kz.yermek.dto.ReconfirmEmailDto;
import kz.yermek.dto.request.JwtRequestDto;
import kz.yermek.dto.request.UserRequestDto;
import kz.yermek.dto.response.JwtResponseDto;
import kz.yermek.dto.response.UserResponseDto;
import kz.yermek.models.Token;
import kz.yermek.models.User;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<UserResponseDto> registerUser(UserRequestDto registrationUserDto);
    Token generateConfirmToken(User user);
    ResponseEntity<JwtResponseDto> authenticate(JwtRequestDto authRequest);
    void revokeAllUserTokens(User user);
    ResponseEntity<JwtRefreshTokenDto> refreshToken(String refreshToken);
    ResponseEntity<String> confirmEmail(String token);
    ResponseEntity<String> resendConfirmation(ReconfirmEmailDto dto);
    void sendWeeklyConfirmEmail();
}
