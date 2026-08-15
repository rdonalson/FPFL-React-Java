package com.financialplanner.moduleauth.infrastructure.persistence.adapter.custom;

import com.financialplanner.moduleauth.infrastructure.persistence.entity.UserRoles;
import com.financialplanner.moduleauth.infrastructure.persistence.repository.custom.JpaUserRolesCustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Repository
public class JpaUserRolesCustomRepositoryImpl implements JpaUserRolesCustomRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void replaceRolesForUser(Long userId, List<Long> roleIds) {
        // Validate contract: roleIds must not be null
        if (roleIds == null) {
            throw new IllegalArgumentException("roleIds must not be null. Use an empty list to clear roles.");
        }

        // Delete existing mappings for the user (always run)
        em.createQuery("DELETE FROM UserRoles ur WHERE ur.userId = :userId")
            .setParameter("userId", userId)
            .executeUpdate();

        // If empty list, we are done (this clears roles)
        if (roleIds.isEmpty()) {
            return;
        }

        // Insert new mappings in batches
        final int batchSize = 50;
        int i = 0;
        for (Long roleId : roleIds) {
            UserRoles ur = new UserRoles(userId, roleId);
            em.persist(ur);

            if (++i % batchSize == 0) {
                em.flush();
                em.clear();
            }
        }

        em.flush();
        em.clear();
    }
}
