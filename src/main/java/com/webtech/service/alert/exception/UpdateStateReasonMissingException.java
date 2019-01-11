package com.webtech.service.alert.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UpdateStateReasonMissingException extends Exception {

  public UpdateStateReasonMissingException(String message) {
    super(message);
  }

}
