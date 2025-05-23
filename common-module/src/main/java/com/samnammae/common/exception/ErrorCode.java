package com.samnammae.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Auth Service
    DUPLICATE_EMAIL(400, "이미 등록된 이메일입니다."),
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(401, "비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(401, "토큰이 유효하지 않습니다."),
    TOKEN_MISSING(401, "토큰이 존재하지 않습니다.");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}