package com.gravifox.domain.admin.service.impl;

import com.gravifox.domain.admin.domain.Term;
import com.gravifox.domain.admin.dto.NewTerm;
import com.gravifox.domain.admin.repository.TermRepository;
import com.gravifox.domain.admin.service.TermService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TermServiceImpl implements TermService {
    @Autowired
    private TermRepository termRepository;

    @Override
    public String createTerm(NewTerm newTerm) {
        Term term = Term.builder()
                .termTitle(newTerm.getTermTitle())
                .termKey(newTerm.getTermKey())
                .termContent(newTerm.getTermContent())
                .termVersion(newTerm.getTermVersion())
                .effectiveAt(newTerm.getEffectiveAt())
                .isRequired(newTerm.getIsRequired())
                .build();

        termRepository.save(term);
        return term.getTermTitle();
    }
}
