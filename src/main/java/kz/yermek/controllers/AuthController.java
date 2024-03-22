package kz.yermek.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kz.yermek.dto.JwtRefreshTokenDto;
import kz.yermek.dto.ReconfirmEmailDto;
import kz.yermek.dto.request.JwtRequestDto;
import kz.yermek.dto.request.UserRequestDto;
import kz.yermek.dto.response.JwtResponseDto;
import kz.yermek.dto.response.UserResponseDto;
import kz.yermek.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    private final UserService userService;

    @Operation(
            summary = "Login",
            description = "Endpoint for get token after login"

    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully returned a token"),
            @ApiResponse(responseCode = "403", description = "Username or password is invalid"),
            @ApiResponse(responseCode = "403", description = "Username is enabled")
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@RequestBody JwtRequestDto authRequest) {
        return userService.authenticate(authRequest);
    }

    @Operation(
            summary = "Registration",
            description = "Endpoint for customer to register a new account"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "The provided name is already taken"),
            @ApiResponse(responseCode = "409", description = "The provided email is already taken")
    })

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRequestDto registrationUserDto) {
        return userService.registerUser(registrationUserDto);
    }

    @Operation(
            summary = "Refresh the token",
            description = "If the token is expired then it is possible to generate a new access token using refresh token"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully returned a new access token"),
            @ApiResponse(responseCode = "403", description = "Token has expired"),
            @ApiResponse(responseCode = "403", description = "Token not found"),

    })

    @PostMapping("/refresh-token")
    public ResponseEntity<JwtRefreshTokenDto> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        return userService.refreshToken(refreshToken);
    }

    @Operation(
            summary = "Confirm the email",
            description = "Whenever a user is registered he or she gets email containing link to activate his or her account"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email successfully confirmed"),
            @ApiResponse(responseCode = "403", description = "Token has expired"),
            @ApiResponse(responseCode = "403", description = "Token not found")


    })
    @Hidden
    @GetMapping("/confirm-email")
    public ResponseEntity<String> confirmEmail(@RequestParam("token") String token) {
        return userService.confirmEmail(token);
    }

    @Operation(
            summary = "Reconfirm email",
            description = "User can get another link to confirm their email"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email successfully confirmed"),
            @ApiResponse(responseCode = "403", description = "Token has expired"),
            @ApiResponse(responseCode = "403", description = "Token not found")

    })
    @Hidden
    @PostMapping("/reconfirm-email")
    public ResponseEntity<String> reconfirm(@RequestBody ReconfirmEmailDto dto) {
        return userService.resendConfirmation(dto);
    }

    @Operation(
            summary = "Logout",
            description = "To logout from the system using this endpoint"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),

    })
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.status(HttpStatus.OK).body("Logged out successfully");
    }
}
