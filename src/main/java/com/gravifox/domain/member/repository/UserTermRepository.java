package com.gravifox.domain.member.repository;

import com.gravifox.domain.member.domain.UserTerm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTermRepository extends JpaRepository<UserTerm, Long> {
}
