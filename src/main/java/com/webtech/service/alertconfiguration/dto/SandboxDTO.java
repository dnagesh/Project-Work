package com.webtech.service.alertconfiguration.dto;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("sandbox")
public class SandboxDTO {

  @PrimaryKey
  private UUID uuid;

  private String name;

  private String status;

  private String owner;

  private Instant createdWhen;

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public Instant getCreatedWhen() {
    return createdWhen;
  }

  public void setCreatedWhen(Instant createdWhen) {
    this.createdWhen = createdWhen;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
