package com.webtech.service.alert.dto;

import java.util.UUID;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class SandboxAlertAuditDTOPrimaryKey {

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 1)
  private UUID alertId;

  @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2, ordering = Ordering.DESCENDING)
  private UUID auditId;


  public SandboxAlertAuditDTOPrimaryKey(UUID alertId, UUID auditId) {
    this.alertId = alertId;
    this.auditId = auditId;
  }

  public UUID getAlertId() {
    return alertId;
  }

  public UUID getAuditId() {
    return auditId;
  }

}
