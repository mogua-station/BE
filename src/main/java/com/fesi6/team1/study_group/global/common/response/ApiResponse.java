package com.fesi6.team1.study_group.global.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {

    private static final String SUCCESS_STATUS = "success";
    private static final String ERROR_STATUS = "error";
    private static final String TOKEN_ERROR_STATUS = "invalid-token";

    private String status;
    private T data;
    private String message;

    public static <T> ApiResponse<T> successResponse(T data) {
        return new ApiResponse<>(SUCCESS_STATUS, data, null);
    }

    public static ApiResponse<?> successWithNoContent() {
        return new ApiResponse<>(SUCCESS_STATUS, null, null);
    }

    public static ApiResponse<?> successWithMessage(String message) {
        return new ApiResponse<>(SUCCESS_STATUS, null, message);
    }

    public static <T> ApiResponse<T> successWithDataAndMessage(T data, String message) {
        return new ApiResponse<>(SUCCESS_STATUS, data, message);
    }

    public static ApiResponse<?> errorResponse(String message) {
        return new ApiResponse<>(ERROR_STATUS, null, message);
    }

    public static ApiResponse<?> tokenErrorResponse(String message) {
        return new ApiResponse<>(TOKEN_ERROR_STATUS, null, message);
    }

    private ApiResponse(String status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }
}
