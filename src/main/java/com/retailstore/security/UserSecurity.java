package com.retailstore.security;

import com.retailstore.security.userdetails.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserSecurity {

    public boolean isOwnerByUserId(Long userId, Authentication authentication){

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)){
            return false;
        }

        return userDetails.getId().equals(userId);
    }
}
