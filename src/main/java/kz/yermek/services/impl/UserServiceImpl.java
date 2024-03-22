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
    private static final String EMAIL_LINK = "http://localhost:8080/api/v1/auth/confirm-email?token=";

    @Override
    @Transactional
    public ResponseEntity<UserResponseDto> registerUser(UserRequestDto userRequestDto) {
        if (userRepository.findByEmail(userRequestDto.email()).isPresent()) {
            throw new EmailAlreadyExistException("Email already exist");
        }

        User user = new User();
        user.setEnabled(false);
        user.setEmail(userRequestDto.email());
        user.setName(userRequestDto.name());

        Role role = roleService.getUserRole().orElseThrow(() ->
                new UserRoleNotFoundException("Role not found"));
        user.setRoles(Collections.singletonList(role));
        String password = userRequestDto.password();
        String confirmPassword = userRequestDto.confirmPassword();

        if (!password.equals(confirmPassword)) {
            throw new PasswordDontMatchException("Passwords don't match");
        }
        user.setPassword(passwordEncoder.encode(userRequestDto.password()));
        userRepository.save(user);

        Token token = generateToken(user);
        tokenService.saveToken(token);

        String link = EMAIL_LINK + token.getToken();
        emailService.sendConfirmationEmail(link, user);

        return ResponseEntity.ok(new UserResponseDto("Successfully! Check your email for confirmation", user.getUsername()));
    }


    @Override
    public ResponseEntity<JwtResponseDto> authenticate(JwtRequestDto jwtRequestDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(jwtRequestDto.email(), jwtRequestDto.password()));
            User user = (User) authentication.getPrincipal();
            String accessToken = jwtTokenUtils.generateAccessToken(user);
            String refreshToken = jwtTokenUtils.generateRefreshToken(user);
            return ResponseEntity.ok(new JwtResponseDto(accessToken, refreshToken));
        } catch (AuthenticationException exception) {
            if (exception instanceof BadCredentialsException) {
                throw new BadCredentialsException("Invalid email or password");
            } else {
                throw new DisabledException("User not enabled yet");
            }
        }
    }

    @Override
    public ResponseEntity<JwtRefreshTokenDto> refreshToken(String refreshToken) {
        try {
            if (refreshToken == null) {
                return ResponseEntity.badRequest().build();
            }
            String usernameFromRefreshToken = jwtTokenUtils.getEmailFromRefreshToken(refreshToken);
            if (usernameFromRefreshToken == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            User user = userRepository.findByEmail(usernameFromRefreshToken).orElseThrow(() ->
                    new UserRoleNotFoundException("User not found"));

            String accessToken = jwtTokenUtils.generateAccessToken(user);
            return ResponseEntity.ok(new JwtRefreshTokenDto(usernameFromRefreshToken, accessToken));
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> confirmEmail(String token) {
        Token confirmationToken = tokenService.getToken(token).orElseThrow(() -> new TokenNotFoundException("Token not found"));
        if (confirmationToken.getConfirmedAt() != null) {
            throw new EmailAlreadyConfirmedException("Email already confirmed");
        }
        LocalDateTime expiredAt = confirmationToken.getExpiresAt();
        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Token has expired");

        }
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        User user = confirmationToken.getUser();
        user.setEnabled(true);
        userRepository.saveAndFlush(user);
        tokenRepository.saveAndFlush(confirmationToken);

        return ResponseEntity.ok().body("Email successfully confirmed. Go back to your login page");
    }

    @Override
    public Token generateToken(User user) {
        String token = UUID.randomUUID().toString();
        Token confirmationToken = new Token(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(5),
                null,
                user);
        return confirmationToken;
    }

    @Override
    public ResponseEntity<String> resendConfirmation(ReconfirmEmailDto emailDto) {
        User user = userRepository.findByEmail(emailDto.email()).orElseThrow(
                () -> new UsernameNotFoundException("User not found")
        );

        if (user.isEnabled()) {
            throw new EmailAlreadyConfirmedException("Email already confirmed");
        }

        List<Token> confirmationTokens = tokenRepository.findByUser(user);
        for (Token token : confirmationTokens) {
            token.setToken(null);
            tokenRepository.save(token);
        }

        Token newConfirmationToken = generateToken(user);
        tokenRepository.save(newConfirmationToken);
        String link = EMAIL_LINK + newConfirmationToken.getToken();
        emailService.sendConfirmationEmail(link, user);
        return ResponseEntity.ok("Successfully! Check your email to the reconfirm process :)");
    }

    @Override
    public boolean isFollowed(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UsernameNotFoundException("Current user not found"));
        List<User> followings = currentUser.getFollowings();
        return followings.contains(user);
    }

    @Override
    public ResponseEntity<UserProfileDto> getUserProfile(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<Recipe> recipeListDto = recipeRepository.findRecipesByUserId(userId);
        boolean isFollowed = isFollowed(userId, currentUserId);
        UserProfileDto userProfileDto = new UserProfileDto(
                user.getPhoto().getUrl(),
                user.getUsername(),
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

        for (User user : users) {
            UserDto dto = new UserDto(
                    user.getPhoto().getUrl(), user.getUsername()
            );
            userDto.add(dto);
        }
        return userDto;
    }

    @Override
    @Transactional
    public void changeProfile(UserUpdateProfileDto dto, MultipartFile photo, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setName(dto.name());
        user.setBio(dto.bio());
        if (photo != null && !photo.isEmpty()) {
            String imageUrl = imageService.saveUserImage(photo).getUrl();
            Image image = new Image();
            image.setUrl(imageUrl);
            user.setPhoto(image);
        }
        userRepository.save(user);
    }

    @Override
    public ResponseEntity<MyProfileDto> getOwnProfile(Long currentUserId) {
        User user = userRepository.findById(currentUserId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<Recipe> recipeListDto = recipeRepository.findRecipesByUserId(currentUserId);
        MyProfileDto userProfileDto = new MyProfileDto(
                user.getPhoto().getUrl(),
                user.getUsername(),
                recipeListDto.size(),
                user.getFollowers().size(),
                user.getFollowings().size(),
                user.getBio()
        );
        return ResponseEntity.ok(userProfileDto);
    }

    @Scheduled(cron = "0 0 12 * * MON")
    private void sendWeeklyConfirmEmail() {
        List<User> users = userRepository.findNotEnabledUsers();
        for (User user : users) {
            Token confirmationToken = generateToken(user);
            tokenService.saveToken(confirmationToken);

            String link = EMAIL_LINK + confirmationToken.getToken();
            emailService.sendConfirmationEmail(link, user);
        }
    }
}
