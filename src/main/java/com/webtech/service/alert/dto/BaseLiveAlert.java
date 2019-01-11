package com.webtech.service.alert.dto;

import java.util.UUID;

public abstract class BaseLiveAlert extends BaseAlert {

  private UUID runId;
  private UUID sandboxAlertId;

  public UUID getRunId() {
    return runId;
  }

  public void setRunId(UUID runId) {
    this.runId = runId;
  }

  public UUID getSandboxAlertId() {
    return sandboxAlertId;
  }

  public void setSandboxAlertId(UUID sandboxAlertId) {
    this.sandboxAlertId = sandboxAlertId;
  }

  @Override
  public String toString() {
    return "BaseLiveAlert{" +
        "runId=" + runId +
        ", sandboxAlertId=" + sandboxAlertId +
        "} " + super.toString();
  }
}
