package com.example.demo.exception;

public class StudentNotEnrolledException extends RuntimeException {

  public StudentNotEnrolledException(String message) {
    super(message);
  }
}
