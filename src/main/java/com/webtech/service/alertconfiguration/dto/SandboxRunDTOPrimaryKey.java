package com.webtech.service.alertconfiguration.dto;

import java.util.Objects;
import java.util.UUID;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class SandboxRunDTOPrimaryKey {


  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 1)
  private UUID sandboxUUID;

  @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2)
  private UUID runUUID;

  public UUID getSandboxUUID() {
    return sandboxUUID;
  }

  public void setSandboxUUID(UUID sandboxUUID) {
    this.sandboxUUID = sandboxUUID;
  }

  public UUID getRunUUID() {
    return runUUID;
  }

  public void setRunUUID(UUID runUUID) {
    this.runUUID = runUUID;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SandboxRunDTOPrimaryKey that = (SandboxRunDTOPrimaryKey) o;
    return Objects.equals(sandboxUUID, that.sandboxUUID) &&
        Objects.equals(runUUID, that.runUUID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sandboxUUID, runUUID);
  }
}
