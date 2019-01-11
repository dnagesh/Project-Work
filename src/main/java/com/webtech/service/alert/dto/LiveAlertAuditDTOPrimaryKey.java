package com.webtech.service.alert.dto;

import java.util.UUID;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class LiveAlertAuditDTOPrimaryKey {

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 1)
  private UUID alertId;

  @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2)
  private UUID auditId;

  public UUID getAlertId() {
    return alertId;
  }

  public void setAlertId(UUID alertId) {
    this.alertId = alertId;
  }

  public UUID getAuditId() {
    return auditId;
  }

  public void setAuditId(UUID auditId) {
    this.auditId = auditId;
  }

  @Override
  public String toString() {
    return "LiveAlertAuditDTOPrimaryKey{" +
        "alertId=" + alertId +
        ", auditId=" + auditId +
        '}';
  }
}
