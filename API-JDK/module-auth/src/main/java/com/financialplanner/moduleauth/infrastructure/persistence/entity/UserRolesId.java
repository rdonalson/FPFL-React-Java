package com.financialplanner.moduleauth.infrastructure.persistence.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class UserRolesId implements Serializable {

    private Long userId;
    private Long roleId;

    public UserRolesId() {}

    public UserRolesId(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }
}
