package com.gravifox.tvb.domain.admin.repository;

import com.gravifox.tvb.domain.admin.domain.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.repository.CrudRepository;

public interface TermRepository extends JpaRepository<Term, Long> {
}

