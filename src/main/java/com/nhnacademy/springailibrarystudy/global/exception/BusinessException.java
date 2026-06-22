package com.nhnacademy.springailibrarystudy.global.exception;

public class BusinessException extends RuntimeException {
  public BusinessException(String message) {
    super(message);
  }
}
