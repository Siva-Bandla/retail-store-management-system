package com.retailstore.auth.service;

import com.retailstore.user.entity.User;
import com.retailstore.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserSecurityService {

    private static final Logger log = LoggerFactory.getLogger(UserSecurityService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserSecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Value("${security.max-failed-attempts}")
    private int maxFailedAttempts;

    @Profile("!test")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFailedLogin(User user, String email) {

        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);

        if(attempts >= maxFailedAttempts){

            user.setAccountLocked(true);
            user.setLockTime(LocalDateTime.now());

            log.warn("User {} account locked after {} failed attempts", email, attempts);
        }
        userRepository.save(user);
    }
}
