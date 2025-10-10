package com.gravifox.domain.member.repository;

import com.gravifox.domain.member.domain.Password;
import com.gravifox.domain.member.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordRepository extends JpaRepository<Password, Long> {

    @Query("SELECT p.password FROM Password p WHERE p.user = :user")
    Optional<String> findPasswordByUser(@Param("user") User user);

    void deleteByUser(User user);
    Optional<Password> findByUser(User user);
}
