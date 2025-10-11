//package com.gravifox.api.domain.member.repo;
//
//import com.gravifox.domain.admin.domain.Term;
//import com.gravifox.domain.member.domain.UserTerm;
//import com.gravifox.domain.member.repository.UserTermRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
//import org.springframework.test.context.ActiveProfiles;
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.time.LocalDateTime;
//
//@DataJpaTest
//@ActiveProfiles("test")
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//public class UserTermRepositoryTest {
//    @Autowired
//    UserTermRepository userTermRepository;
//
//    @Autowired
//    TestEntityManager entityManager;
//
//    Term term;
//
//    @BeforeEach
//    public void setUp() {
//        term = Term.builder()
//                .termKey("tos")
//                .termTitle("Terms of Service")
//                .termVersion("1.0")
//                .termContent("서비스 이용약관 내용입니다.")
//                .isRequired(true)
//                .effectiveAt(LocalDateTime.now())
//                .build();
//        entityManager.persist(term);
//        entityManager.flush();
//    }
//    @Test
//    public void saveUserTermEntity() {
//        UserTerm userTerm = UserTerm.builder()
//                .userNo(32L)
//                .termId(term.getTermId())
//                .agreedVersion(term.getTermVersion())
//                .build();
//
//        entityManager.persist(userTerm);
//        entityManager.flush();
//
//        UserTerm saved = userTermRepository.save(userTerm);
//
//        assertThat(saved.getTermId()).isEqualTo(term.getTermId());
//        assertThat(saved.getAgreedVersion()).isEqualTo(term.getTermVersion());
//    }
//}
