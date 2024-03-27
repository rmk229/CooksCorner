package kz.yermek.services.impl;

import jakarta.transaction.Transactional;
import kz.yermek.dto.JwtRefreshTokenDto;
import kz.yermek.dto.ReconfirmEmailDto;
import kz.yermek.dto.request.JwtRequestDto;
import kz.yermek.dto.request.UserRequestDto;
import kz.yermek.dto.response.JwtResponseDto;
import kz.yermek.dto.response.UserResponseDto;
import kz.yermek.enums.TokenType;
import kz.yermek.exceptions.*;
import kz.yermek.models.AccessToken;
import kz.yermek.models.Role;
import kz.yermek.models.Token;
import kz.yermek.models.User;
import kz.yermek.repositories.AccessTokenRepository;
import kz.yermek.repositories.TokenRepository;
import kz.yermek.repositories.UserRepository;
import kz.yermek.services.AuthService;
import kz.yermek.services.EmailService;
import kz.yermek.services.RoleService;
import kz.yermek.services.TokenService;
import kz.yermek.util.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final TokenService confirmationTokenService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final AccessTokenRepository accessTokenRepository;
    private final TokenRepository confirmationTokenRepository;
    private static final String CONFIRM_EMAIL_LINK = System.getenv("CONFIRM_EMAIL_LINK");

    @Autowired
    public AuthServiceImpl(RoleService roleService, PasswordEncoder passwordEncoder, UserRepository userRepository, JwtTokenUtils jwtTokenUtils, TokenService confirmationTokenService, AuthenticationManager authenticationManager, EmailService emailService, AccessTokenRepository accessTokenRepository, TokenRepository confirmationTokenRepository) {
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtTokenUtils = jwtTokenUtils;
        this.confirmationTokenService = confirmationTokenService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.accessTokenRepository = accessTokenRepository;
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    @Override
    @Transactional
    public ResponseEntity<UserResponseDto> registerUser(UserRequestDto registrationUserDto) {

        if (userRepository.findByEmail(registrationUserDto.email()).isPresent()) {
            throw new EmailAlreadyExistException("Email already exist. Please, try to use another one.");
        }
        User user = new User();
        user.setEnabled(false);
        user.setEmail(registrationUserDto.email());
        user.setName(registrationUserDto.name());
        Role userRole = roleService.getUserRole()
                .orElseThrow(() -> new UserRoleNotFoundException("Role not found."));
        user.setRoles(Collections.singletonList(userRole));
        String password = registrationUserDto.password();
        String confirmPassword = registrationUserDto.confirmPassword();
        if (!password.equals(confirmPassword)) {
            throw new PasswordDontMatchException("Passwords do not match.");
        }
        user.setPassword(passwordEncoder.encode(registrationUserDto.password()));
        userRepository.save(user);
        Token confirmationToken = generateConfirmToken(user);
        confirmationTokenService.saveToken(confirmationToken);
        String link = CONFIRM_EMAIL_LINK + confirmationToken.getToken();
        emailService.sendConfirmationEmail(link, user);
        return ResponseEntity.ok(new UserResponseDto("Success! Please, check your email for the confirmation", user.getUsername()));
    }

    @Override
    public Token generateConfirmToken(User user) {
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
    @Transactional
    public ResponseEntity<JwtResponseDto> authenticate(JwtRequestDto authRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password()));
            User user = (User) authentication.getPrincipal();
            String accessToken = jwtTokenUtils.generateAccessToken(user);
            String refreshToken = jwtTokenUtils.generateRefreshToken(user);
            AccessToken token = AccessToken.builder()
                    .user(user)
                    .token(accessToken)
                    .tokenType(TokenType.BEARER)
                    .revoked(false)
                    .expired(false)
                    .build();
            revokeAllUserTokens(user);
            accessTokenRepository.save(token);
            return ResponseEntity.ok(new JwtResponseDto(accessToken, refreshToken));
        } catch (AuthenticationException exception) {
            if (exception instanceof BadCredentialsException) {
                throw new BadCredentialsException("Invalid email or password");
            } else {
                throw new DisabledException("User is not enabled yet");
            }
        }
    }

    @Override
    public void revokeAllUserTokens(User user) {
        List<AccessToken> validUserTokens = accessTokenRepository.findAllValidTokensByUser(user.getId());
        if(validUserTokens.isEmpty()){
            return;
        }
        validUserTokens.forEach(t ->{
            t.setExpired(true);
            t.setRevoked(true);
        });
        accessTokenRepository.saveAll(validUserTokens);
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
                    new UsernameNotFoundException("User not found"));
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
        Token confirmationToken = confirmationTokenService.getToken(token).orElseThrow(()->new TokenNotFoundException("Token not found"));
        if (confirmationToken.getConfirmedAt() != null) {
            throw new EmailAlreadyConfirmedException("Email already confirmed");
        }
        LocalDateTime expiredAt = confirmationToken.getExpiresAt();
        if(expiredAt.isBefore(LocalDateTime.now())){
            throw new TokenExpiredException("Token has expired");

        }
        confirmationToken.setConfirmedAt(LocalDateTime.now());
        User user = confirmationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        confirmationTokenRepository.save(confirmationToken);

        return ResponseEntity.ok().body("Email successfully confirmed. Go back to your login page");
    }

    @Override
    public ResponseEntity<String> resendConfirmation(ReconfirmEmailDto dto) {
        User user = userRepository.findByEmail(dto.email()).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));
        if(user.isEnabled()){
            throw new EmailAlreadyConfirmedException("Email already confirmed");
        }

        List<Token> confirmationTokens = confirmationTokenRepository.findByUser(user);
        for(Token confirmationToken : confirmationTokens){
            confirmationToken.setToken(null);
            confirmationTokenRepository.save(confirmationToken);
        }


        Token newConfirmationToken = generateConfirmToken(user);
        confirmationTokenRepository.save(newConfirmationToken);
        String link = CONFIRM_EMAIL_LINK + newConfirmationToken.getToken();
        emailService.sendConfirmationEmail(link, user);
        return ResponseEntity.ok("Success! Please, check your email for the re-confirmation");
    }
    @Scheduled(cron = "0 0 12 * * MON")
    @Override
    public void sendWeeklyConfirmEmail() {
        List<User> users = userRepository.findNotEnabledUsers();
        for(User user: users){
            Token confirmationToken = generateConfirmToken(user);
            confirmationTokenService.saveToken(confirmationToken);
            String link = CONFIRM_EMAIL_LINK + confirmationToken.getToken();
            emailService.sendConfirmationEmail(link, user);
        }
    }
}
