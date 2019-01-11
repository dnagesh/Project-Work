package com.webtech.service.alert.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import java.util.UUID;
import org.junit.Test;

public class SandboxAlertAuditDTOPrimaryKeyTest {

  @Test
  public void parameterisedConstructor() {
    UUID alertId = TestUtils.randomUUID();
    UUID auditId = TestUtils.randomUUID();
    SandboxAlertAuditDTOPrimaryKey primaryKey = new SandboxAlertAuditDTOPrimaryKey(alertId,
        auditId);
    assertThat(primaryKey).isNotNull();
    assertThat(primaryKey.getAlertId()).isEqualTo(alertId);
    assertThat(primaryKey.getAuditId()).isEqualTo(auditId);
  }
}
