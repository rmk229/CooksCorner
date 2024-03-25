package kz.yermek.dto;

import java.io.Serializable;

public record UserDto(Long id, String name, String photoUrl) implements Serializable {
}
