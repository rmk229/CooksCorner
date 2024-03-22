package kz.yermek.dto;

import java.io.Serializable;

public record UserDto(String name, String photoUrl) implements Serializable {
}
