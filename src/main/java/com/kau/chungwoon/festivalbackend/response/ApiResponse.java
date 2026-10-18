package com.kau.chungwoon.festivalbackend.response;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        ErrorResponse error
) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "SUCCESS", message, data, null);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, "SUCCESS", message, null, null);
    }

    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(false, code, message, null, ErrorResponse.of(code, message));
    }

    public static ApiResponse<Void> fail(String code, String message, ErrorResponse errorResponse) {
        return new ApiResponse<>(false, code, message, null, errorResponse);
    }
}
