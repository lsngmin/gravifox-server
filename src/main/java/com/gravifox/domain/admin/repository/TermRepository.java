package com.gravifox.domain.admin.repository;

import com.gravifox.domain.admin.domain.Term;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long> {
}
