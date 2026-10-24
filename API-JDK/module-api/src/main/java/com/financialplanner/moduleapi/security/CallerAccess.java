package com.financialplanner.moduleapi.security;

import com.financialplanner.modulecommonbc.exception.ForbiddenOperationException;
import com.financialplanner.modulecommonbc.exception.InvalidCredentialsException;

import java.util.UUID;

/**
 * Who is calling, and may they touch this data? Everything a user owns is keyed by their {@code userId}
 * (UUID); admins may act on anyone's.
 */
public final class CallerAccess {

    public static final String ADMIN_ROLE = "ROLE_ADMIN";

    private CallerAccess() {}

    /** The signed-in user's id. */
    public static UUID userId(CustomUserDetails principal) {
        if (principal == null || principal.getUser() == null || principal.getUser().getUserId() == null) {
            throw new InvalidCredentialsException("Sign in to continue");
        }
        return principal.getUser().getUserId();
    }

    public static boolean isAdmin(CustomUserDetails principal) {
        return principal != null && principal.getRoleNames() != null && principal.getRoleNames().contains(ADMIN_ROLE);
    }

    /** True when {@code ownerUserId} is the caller, or the caller is an admin. */
    public static boolean canAccess(UUID ownerUserId, CustomUserDetails principal) {
        return isAdmin(principal) || userId(principal).equals(ownerUserId);
    }

    /** 403 unless {@code ownerUserId} is the caller (or the caller is an admin). */
    public static void requireSelfOrAdmin(UUID ownerUserId, CustomUserDetails principal) {
        if (!canAccess(ownerUserId, principal)) {
            throw new ForbiddenOperationException("You can only access your own data");
        }
    }

    /** 403 unless the caller is an admin. */
    public static void requireAdmin(CustomUserDetails principal) {
        userId(principal);
        if (!isAdmin(principal)) {
            throw new ForbiddenOperationException("Only an administrator can do that");
        }
    }
}
