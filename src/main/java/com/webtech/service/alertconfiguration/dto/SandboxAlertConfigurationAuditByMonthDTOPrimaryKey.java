package com.webtech.service.alertconfiguration.dto;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class SandboxAlertConfigurationAuditByMonthDTOPrimaryKey {

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 1)
  private LocalDate whenMonth;

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 2)
  private UUID sandboxUUID;

  @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 3, ordering = Ordering.DESCENDING)
  private UUID auditUUID;

  public SandboxAlertConfigurationAuditByMonthDTOPrimaryKey(
      LocalDate whenMonth, UUID sandboxUUID, UUID auditUUID) {
    this.whenMonth = whenMonth;
    this.sandboxUUID = sandboxUUID;
    this.auditUUID = auditUUID;
  }

  public SandboxAlertConfigurationAuditByMonthDTOPrimaryKey() {
  }

  public LocalDate getWhenMonth() {
    return whenMonth;
  }

  public void setWhenMonth(LocalDate whenMonth) {
    this.whenMonth = whenMonth;
  }

  public UUID getSandboxUUID() {
    return sandboxUUID;
  }

  public void setSandboxUUID(UUID sandboxUUID) {
    this.sandboxUUID = sandboxUUID;
  }

  public UUID getAuditUUID() {
    return auditUUID;
  }

  public void setAuditUUID(UUID auditUUID) {
    this.auditUUID = auditUUID;
  }

}
