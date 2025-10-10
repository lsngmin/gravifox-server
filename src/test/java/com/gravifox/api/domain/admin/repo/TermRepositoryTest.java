package com.gravifox.api.domain.admin.repo;

import com.gravifox.domain.admin.domain.Term;
import com.gravifox.domain.admin.repository.TermRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TermRepositoryTest {
    @Autowired
    TermRepository termRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    public void saveTermEntity() {
        Term term = Term.builder()
                .termKey("tos")
                .termTitle("Terms of Service")
                .termVersion("1.0")
                .termContent("서비스 이용약관 내용입니다.")
                .isRequired(true)
                .effectiveAt(LocalDateTime.now())
                .build();
        entityManager.persist(term);
        entityManager.flush();

        Term saved = termRepository.save(term);

        assertThat(saved.getTermId()).isNotNull();
        assertThat(saved.getTermKey()).isEqualTo("tos");
        assertThat(saved.getTermTitle()).isEqualTo("Terms of Service");
    }
}
