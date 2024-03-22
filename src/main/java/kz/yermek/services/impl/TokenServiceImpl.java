package kz.yermek.services.impl;

import kz.yermek.models.Token;
import kz.yermek.repositories.TokenRepository;
import kz.yermek.services.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final TokenRepository tokenRepository;
    @Override
    public void saveToken(Token token) {
        tokenRepository.save(token);
    }

    @Override
    public Optional<Token> getToken(String token) {
        return tokenRepository.findByToken(token);
    }
}
