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

    // Menu Service
    STORE_ACCESS_DENIED(403, "접근 권한이 없는 매장입니다."),
    CATEGORY_ID_EMPTY(400, "수정할 카테고리 ID가 없습니다."),
    CATEGORY_NOT_FOUND(404, "해당 카테고리를 찾을 수 없습니다."),
    CATEGORY_ACCESS_DENIED(403, "해당 카테고리에 대한 권한이 없습니다."),
    MENU_CATEGORY_NOT_FOUND(404, "메뉴 카테고리를 찾을 수 없습니다."),
    MENU_NOT_FOUND(404, "메뉴를 찾을 수 없습니다."),
    MENU_STORE_MISMATCH(403, "해당 매장의 메뉴가 아닙니다."),
    INVALID_OPTION_CATEGORY_FORMAT( 400, "옵션 카테고리 형식이 잘못되었습니다."),
    INVALID_FILE(400, "업로드할 파일이 비어있습니다."),
    INVALID_FILE_NAME(400, "파일 이름이 없습니다."),
    FILE_IO_ERROR(500, "파일 입출력 오류가 발생했습니다."),
    S3_SERVICE_ERROR(500, "S3 서비스 오류가 발생했습니다."),

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