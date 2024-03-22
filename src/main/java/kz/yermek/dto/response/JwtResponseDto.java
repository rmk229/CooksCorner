package kz.yermek.dto.response;

import lombok.Builder;

@Builder
public record JwtResponseDto (String accessToken, String refreshToken){
}
