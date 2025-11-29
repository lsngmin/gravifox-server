package com.gravifox.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 공통 에러 코드 계약. 서비스별 enum이 구현해 사용한다.
 */
public interface ErrorCode {

    /**
     * 이 에러에 매핑될 HTTP 상태. 클라이언트로 그대로 노출된다.
     */
    HttpStatus getHttpStatus();

    /**
     * 모니터링/로깅용 고유 코드. 서비스 접두어를 포함해 충돌 없이 관리한다.
     */
    String getCode();

    /**
     * 사용자(또는 클라이언트)가 볼 짧은 메시지. 내부 상세는 노출하지 않는다.
     */
    String getMessage();
}
