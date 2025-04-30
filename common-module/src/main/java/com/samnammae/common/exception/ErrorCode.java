package com.samnammae.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Auth Service
    DUPLICATE_EMAIL(400, "이미 등록된 이메일입니다."),
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),

    // 필요한 에러 코드 계속 추가
    ;

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}