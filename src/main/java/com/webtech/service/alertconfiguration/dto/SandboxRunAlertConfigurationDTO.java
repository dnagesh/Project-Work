package com.webtech.service.alertconfiguration.dto;

import java.util.UUID;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

@UserDefinedType("sandbox_alert_configuration_udt")
public class SandboxRunAlertConfigurationDTO {

  private UUID sandboxAlertConfigurationUUID;
  private String name;
  private String appHash;


  public SandboxRunAlertConfigurationDTO(UUID sandboxAlertConfigurationUUID, String name,
      String appHash) {
    this.sandboxAlertConfigurationUUID = sandboxAlertConfigurationUUID;
    this.name = name;
    this.appHash = appHash;
  }

  public SandboxRunAlertConfigurationDTO() {
  }

  public UUID getSandboxAlertConfigurationUUID() {
    return sandboxAlertConfigurationUUID;
  }

  public void setSandboxAlertConfigurationUUID(UUID sandboxAlertConfigurationUUID) {
    this.sandboxAlertConfigurationUUID = sandboxAlertConfigurationUUID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAppHash() {
    return appHash;
  }

  public void setAppHash(String appHash) {
    this.appHash = appHash;
  }

  @Override
  public String toString() {
    return "SandboxRunAlertConfigurationDTO{" +
        "sandboxAlertConfigurationUUID=" + sandboxAlertConfigurationUUID +
        ", name='" + name + '\'' +
        ", appHash='" + appHash + '\'' +
        '}';
  }
}
