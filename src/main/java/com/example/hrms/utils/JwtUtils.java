package com.example.hrms.utils;

import com.example.hrms.entities.MyUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class JwtUtils {

    public static Long getUserId(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();

        Long userId = myUserDetails.getId();
        if(userId == null){
            userId = null;
        }

        return userId;
    }
}
