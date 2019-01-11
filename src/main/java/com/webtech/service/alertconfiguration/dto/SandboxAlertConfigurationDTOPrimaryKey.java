package com.webtech.service.alertconfiguration.dto;

import java.util.Objects;
import java.util.UUID;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class SandboxAlertConfigurationDTOPrimaryKey {

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 1)
  private UUID sandboxUUID;

  @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2)
  private UUID alertConfigurationUUID;

  public SandboxAlertConfigurationDTOPrimaryKey() {
  }

  public SandboxAlertConfigurationDTOPrimaryKey(UUID sandboxUUID,
      UUID alertConfigurationUUID) {
    this.sandboxUUID = sandboxUUID;
    this.alertConfigurationUUID = alertConfigurationUUID;
  }

  public UUID getSandboxUUID() {
    return sandboxUUID;
  }

  public void setSandboxUUID(UUID sandboxUUID) {
    this.sandboxUUID = sandboxUUID;
  }

  public UUID getAlertConfigurationUUID() {
    return alertConfigurationUUID;
  }

  public void setAlertConfigurationUUID(UUID alertConfigurationUUID) {
    this.alertConfigurationUUID = alertConfigurationUUID;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SandboxAlertConfigurationDTOPrimaryKey that = (SandboxAlertConfigurationDTOPrimaryKey) o;
    return Objects.equals(sandboxUUID, that.sandboxUUID) &&
        Objects.equals(alertConfigurationUUID, that.alertConfigurationUUID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sandboxUUID, alertConfigurationUUID);
  }
}
