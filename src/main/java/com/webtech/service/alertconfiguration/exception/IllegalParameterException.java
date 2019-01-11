package com.webtech.service.alertconfiguration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalParameterException extends Exception {

  public IllegalParameterException(String message) {
    super(message);
  }
}
