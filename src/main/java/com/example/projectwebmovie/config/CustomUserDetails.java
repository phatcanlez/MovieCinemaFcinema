package com.example.projectwebmovie.config;

import com.example.projectwebmovie.model.Account;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final Account account;

    public CustomUserDetails(Account account) {
        this.account = account;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = switch (account.getRoleId()) {
            case 1 -> "ROLE_ADMIN";
            case 2 -> "ROLE_EMPLOYEE";
            case 3 -> "ROLE_USER";
            default -> throw new IllegalStateException("Vai trò không hợp lệ");
        };
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // We’re not implementing account expiration
    }

    @Override
    public boolean isAccountNonLocked() {
        return account.getStatus() == 1; // Account is locked if status is 0
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // We’re not implementing credential expiration
    }

    @Override
    public boolean isEnabled() {
        return account.getStatus() == 1; // Account is enabled if status is 1
    }

    // Getter for the Account object if needed
    public Account getAccount() {
        return account;
    }
}