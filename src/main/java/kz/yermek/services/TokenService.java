package kz.yermek.services;

import kz.yermek.models.Token;

import java.util.Optional;

public interface TokenService {
    void saveToken(Token token);
    Optional<Token> getToken(String token);
}
