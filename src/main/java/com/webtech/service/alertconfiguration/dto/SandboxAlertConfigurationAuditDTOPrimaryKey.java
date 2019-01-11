package com.webtech.service.alertconfiguration.dto;

import java.util.Objects;
import java.util.UUID;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class SandboxAlertConfigurationAuditDTOPrimaryKey {

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 1)
  private UUID sandboxUUID;

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 2)
  private UUID alertConfigurationUUID;

  @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 3)
  private UUID auditUUID;

  public SandboxAlertConfigurationAuditDTOPrimaryKey() {
  }

  public SandboxAlertConfigurationAuditDTOPrimaryKey(UUID sandboxUUID,
      UUID alertConfigurationUUID, UUID auditUUID) {
    this.sandboxUUID = sandboxUUID;
    this.alertConfigurationUUID = alertConfigurationUUID;
    this.auditUUID = auditUUID;
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

  public UUID getAuditUUID() {
    return auditUUID;
  }

  public void setAuditUUID(UUID auditUUID) {
    this.auditUUID = auditUUID;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SandboxAlertConfigurationAuditDTOPrimaryKey that = (SandboxAlertConfigurationAuditDTOPrimaryKey) o;
    return Objects.equals(sandboxUUID, that.sandboxUUID) &&
        Objects.equals(alertConfigurationUUID, that.alertConfigurationUUID) &&
        Objects.equals(auditUUID, that.auditUUID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sandboxUUID, alertConfigurationUUID, auditUUID);
  }
}
