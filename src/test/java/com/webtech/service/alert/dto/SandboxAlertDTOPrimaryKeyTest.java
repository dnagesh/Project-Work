package com.webtech.service.alert.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import java.util.UUID;
import org.junit.Test;

public class SandboxAlertDTOPrimaryKeyTest {

  @Test
  public void parameterisedConstructor() {
    UUID alertId = TestUtils.randomUUID();
    UUID runId = TestUtils.randomUUID();
    SandboxAlertDTOPrimaryKey primaryKey = new SandboxAlertDTOPrimaryKey(runId, alertId);
    assertThat(primaryKey).isNotNull();
    assertThat(primaryKey.getAlertId()).isEqualTo(alertId);
    assertThat(primaryKey.getRunId()).isEqualTo(runId);
  }
}
