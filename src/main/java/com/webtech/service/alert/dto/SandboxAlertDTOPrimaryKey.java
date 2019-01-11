package com.webtech.service.alert.dto;

import java.util.UUID;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class SandboxAlertDTOPrimaryKey {

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 1)
  private UUID runId;

  @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2)
  private UUID alertId;

  public SandboxAlertDTOPrimaryKey(UUID runId, UUID alertId) {
    this.runId = runId;
    this.alertId = alertId;
  }

  public UUID getRunId() {
    return runId;
  }

  public UUID getAlertId() {
    return alertId;
  }

}
