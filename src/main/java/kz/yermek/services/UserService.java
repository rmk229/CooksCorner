package kz.yermek.services;

import kz.yermek.dto.*;
import kz.yermek.dto.request.JwtRequestDto;
import kz.yermek.dto.request.UserRequestDto;
import kz.yermek.dto.response.JwtResponseDto;
import kz.yermek.dto.response.UserResponseDto;
import kz.yermek.models.Token;
import kz.yermek.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    ResponseEntity<UserResponseDto> registerUser(UserRequestDto userRequestDto);
    ResponseEntity<JwtResponseDto> authenticate(JwtRequestDto jwtRequestDto);
    ResponseEntity<JwtRefreshTokenDto> refreshToken(String refreshToken);
    ResponseEntity<String> confirmEmail(String token);
    Token generateToken(User user);
    ResponseEntity<String> resendConfirmation(ReconfirmEmailDto emailDto);
    boolean isFollowed(Long userId, Long currentUserId);
    ResponseEntity<UserProfileDto> getUserProfile(Long userId, Long currentUserId);
    List<UserDto> searchUser(String query);
    void changeProfile(UserUpdateProfileDto dto, MultipartFile photo, Long id);
    ResponseEntity<MyProfileDto> getOwnProfile(Long currentUserId);
}
