package com.gravifox.domain.file.token;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UploadTokenRepository extends JpaRepository<UploadToken, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from UploadToken t where t.jti = :jti")
    Optional<UploadToken> findByJtiForUpdate(@Param("jti") String jti);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from UploadToken t where t.id = :id")
    Optional<UploadToken> findByIdForUpdate(@Param("id") Long id);

    List<UploadToken> findAllByUploadIdAndStatusIn(String uploadId, Collection<UploadTokenStatus> statuses);
}
