package com.webtech.service.alertconfiguration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AlertConfigurationNotFoundException extends Exception {

  public AlertConfigurationNotFoundException(String alertConfigUUID) {
    super("Failed to find live alert configuration with ID " + alertConfigUUID);
  }

}
