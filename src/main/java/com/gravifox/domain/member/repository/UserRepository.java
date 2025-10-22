package com.gravifox.domain.member.repository;

import com.gravifox.domain.admin.repository.AdminUserRepositoryCustom;
import com.gravifox.domain.member.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, AdminUserRepositoryCustom {
    Optional<User> findByUserId(String userId);
}
