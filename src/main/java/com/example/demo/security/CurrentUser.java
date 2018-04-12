package com.example.demo.security;

import com.example.demo.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

public class CurrentUser extends org.springframework.security.core.userdetails.User {

    private User user;

    public CurrentUser(User user) {
        super(user.getEmail(), user.getPassword(), user.isVerify(),true,true,true, AuthorityUtils.createAuthorityList(user.getUserType().name()));
        this.user=user;
    }

    public User getUser() {
        return user;
    }
}


