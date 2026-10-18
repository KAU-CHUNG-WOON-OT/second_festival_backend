package com.kau.chungwoon.festivalbackend.exception;

import lombok.Getter;

@Getter
public class BusinessException extends CustomException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}
