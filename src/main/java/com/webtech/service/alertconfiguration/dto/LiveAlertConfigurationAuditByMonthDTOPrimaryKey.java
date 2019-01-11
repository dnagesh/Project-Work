package com.webtech.service.alertconfiguration.dto;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class LiveAlertConfigurationAuditByMonthDTOPrimaryKey {

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 1)
  private LocalDate whenMonth;

  @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2, ordering = Ordering.DESCENDING)
  private UUID auditUUID;

  public LiveAlertConfigurationAuditByMonthDTOPrimaryKey(
      LocalDate whenMonth, UUID auditUUID) {
    this.whenMonth = whenMonth;
    this.auditUUID = auditUUID;
  }

  public LiveAlertConfigurationAuditByMonthDTOPrimaryKey() {
  }

  public LocalDate getWhenMonth() {
    return whenMonth;
  }

  public void setWhenMonth(LocalDate whenMonth) {
    this.whenMonth = whenMonth;
  }

  public UUID getAuditUUID() {
    return auditUUID;
  }

  public void setAuditUUID(UUID auditUUID) {
    this.auditUUID = auditUUID;
  }
}
