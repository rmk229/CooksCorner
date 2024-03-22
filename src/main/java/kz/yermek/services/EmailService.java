package kz.yermek.services;

import kz.yermek.models.User;

public interface EmailService {
    void sendConfirmationEmail(String link, User user);
}
