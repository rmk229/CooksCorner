package kz.yermek.services.impl;

import kz.yermek.dto.*;
import kz.yermek.dto.request.JwtRequestDto;
import kz.yermek.dto.request.UserRequestDto;
import kz.yermek.dto.response.JwtResponseDto;
import kz.yermek.dto.response.UserResponseDto;
import kz.yermek.exceptions.*;
import kz.yermek.models.*;
import kz.yermek.repositories.RecipeRepository;
import kz.yermek.repositories.TokenRepository;
import kz.yermek.repositories.UserRepository;
import kz.yermek.services.*;
import kz.yermek.util.EmailSenderConfig;
import kz.yermek.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final JwtTokenUtils jwtTokenUtils;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;
    private final ImageService imageService;
    private final RecipeRepository recipeRepository;
    private static final String EMAIL_LINK = "https://cookscorner-production-6571.up.railway.app/api/v1/auth/confirm-email?token=";

    @Override
    public ResponseEntity<UserProfileDto> getUserProfile(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<Recipe> recipeListDto = recipeRepository.findRecipesPageByUserId(userId);
        boolean isFollowed = isFollowed(userId, currentUserId);

        String photoUrl = (user.getPhoto() != null) ? user.getPhoto().getUrl() : "https://t4.ftcdn.net/jpg/03/32/59/65/240_F_332596535_lAdLhf6KzbW6PWXBWeIFTovTii1drkbT.jpg";

        UserProfileDto userProfileDto = new UserProfileDto(
                user.getId(),
                photoUrl,
                user.getName(),
                recipeListDto.size(),
                user.getFollowers().size(),
                user.getFollowings().size(),
                user.getBio(),
                isFollowed
        );
        return ResponseEntity.ok(userProfileDto);
    }
    @Override
    public List<UserDto> searchUser(String query) {
        List<User> users = userRepository.searchUsers(query);
        List<UserDto> userDto = new ArrayList<>();

        for(User user: users){
            String photoUrl = (user.getPhoto() != null) ? user.getPhoto().getUrl() : "https://t4.ftcdn.net/jpg/03/32/59/65/240_F_332596535_lAdLhf6KzbW6PWXBWeIFTovTii1drkbT.jpg";
            UserDto dto = new UserDto(
                    user.getId(),
                    user.getName(),
                    photoUrl
            );
            userDto.add(dto);
        }
        return userDto;
    }
    @Override
    public ResponseEntity<MyProfileDto> getOwnProfile(Long currentUserId) {
        User user = userRepository.findById(currentUserId).orElseThrow(()-> new UsernameNotFoundException("User not found"));
        List<Recipe> recipeListDto = recipeRepository.findRecipesPageByUserId(currentUserId);
        String photoUrl = (user.getPhoto() != null) ? user.getPhoto().getUrl() : "https://t4.ftcdn.net/jpg/03/32/59/65/240_F_332596535_lAdLhf6KzbW6PWXBWeIFTovTii1drkbT.jpg";
        MyProfileDto userProfileDto = new MyProfileDto(
                photoUrl,
                user.getName(),
                recipeListDto.size(),
                user.getFollowers().size(),
                user.getFollowings().size(),
                user.getBio()
        );
        return ResponseEntity.ok(userProfileDto);
    }
    @Override
    @Transactional
    public String updateUser(UserUpdateProfileDto request, Long currentUserId, MultipartFile image) {
        User user = userRepository.findById(currentUserId).orElseThrow(()-> new UsernameNotFoundException("User not found"));
        user.setName(request.name());
        user.setBio(request.bio());

        if(image!=null){
            user.setPhoto(imageService.saveImage(image));
        }
        userRepository.save(user);
        return "User profile successfully updated";
    }
    public boolean isFollowed(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));
        List<User> followings = currentUser.getFollowings();
        return followings.contains(user);
    }
}
