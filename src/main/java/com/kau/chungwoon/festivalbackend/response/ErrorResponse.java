package com.kau.chungwoon.festivalbackend.response;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<FieldErrorDetail> fieldErrors
) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, List.of());
    }

    public static ErrorResponse of(String code, String message, List<FieldErrorDetail> fieldErrors) {
        return new ErrorResponse(code, message, fieldErrors);
    }

    public record FieldErrorDetail(
            String field,
            Object rejectedValue,
            String reason
    ) {
    }
}
