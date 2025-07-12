package com.samnammae.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Auth Service
    DUPLICATE_EMAIL(400, "이미 등록된 이메일입니다."),
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(401, "비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(401, "토큰이 유효하지 않습니다."),
    TOKEN_MISSING(401, "토큰이 존재하지 않습니다."),

    // Admin Service
    FILE_UPLOAD_FAILED(500, "파일 업로드에 실패했습니다."),
    STORE_NOT_FOUND(404, "매장을 찾을 수 없습니다."),
    FORBIDDEN_ACCESS(403, "접근 권한이 없습니다."),
    FILE_DELETE_FAILED(500, "파일 삭제에 실패했습니다."),

    // Api Gateway
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다."),
    EXPIRED_TOKEN(401, "토큰이 만료되었습니다."),;

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}