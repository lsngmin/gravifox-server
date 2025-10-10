package com.gravifox.domain.member.repository;

import com.gravifox.domain.member.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByNickname(String nickname);
}
