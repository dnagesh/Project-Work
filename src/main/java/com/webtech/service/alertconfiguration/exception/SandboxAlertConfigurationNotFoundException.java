package com.webtech.service.alertconfiguration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SandboxAlertConfigurationNotFoundException extends Exception {

  public SandboxAlertConfigurationNotFoundException(String sandboxUUID, String sandboxConfigUUID) {
    super("Failed to find sandbox alert configuration with sandboxUUID " + sandboxUUID
        + " and sandboxConfigUUID " + sandboxConfigUUID);
  }
}
