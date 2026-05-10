package com.financialplanner.moduleauth.domain.service;

import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;

public interface AuthService {

    User register(String email, String rawPassword, String first, String last);

    User login(String email, String rawPassword);

    User loadUserByEmail(String email);
}
