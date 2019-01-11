package com.webtech.service.alertconfiguration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SandboxNotFoundException extends Exception {

  public SandboxNotFoundException(String message) {
    super(message);
  }
}
