package kz.yermek.dto;

import lombok.Builder;

@Builder
public record JwtRefreshTokenDto(String username, String accessToken) {
}
