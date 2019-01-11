package com.webtech.service.alertconfiguration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AuditNotFoundException extends Exception {

  public AuditNotFoundException(String message) {
    super(message);
  }
}
