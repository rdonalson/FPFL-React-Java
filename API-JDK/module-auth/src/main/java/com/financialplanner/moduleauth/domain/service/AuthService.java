package com.financialplanner.moduleauth.domain.service;

import com.financialplanner.moduleauth.infrastructure.persistence.entity.User;

public interface AuthService {

    User register(String email, String rawPassword);

    User login(String email, String rawPassword);

    User loadUserByEmail(String email);
}
