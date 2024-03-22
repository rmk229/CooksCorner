package kz.yermek.dto;

import java.io.Serializable;

public record UserUpdateProfileDto(String name, String bio) implements Serializable {
}