package com.financialplanner.moduleauth.infrastructure.persistence.repository.custom;

import java.util.List;

public interface JpaUserRolesCustomRepository {
    void replaceRolesForUser(Long userId, List<Long> roleIds);
}
