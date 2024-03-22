package kz.yermek.dto.response;

import lombok.Builder;

@Builder
public record UserResponseDto(String status, String username) {
}
