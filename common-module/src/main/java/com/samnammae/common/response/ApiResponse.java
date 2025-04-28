package com.samnammae.common.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private int code;
    private String message;
    private T data;

    /**
     * 요청이 성공했을 때 응답 생성 (데이터 포함)
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(200)
                .message("요청이 성공적으로 처리되었습니다.")
                .data(data)
                .build();
    }

    /**
     * 요청이 성공했을 때 응답 생성 (데이터 없음)
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 요청이 실패했을 때 응답 생성
     * @param code 에러 코드
     * @param message 에러 메시지
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}