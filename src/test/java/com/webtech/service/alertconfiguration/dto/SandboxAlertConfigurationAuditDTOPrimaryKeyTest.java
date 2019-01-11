package com.webtech.service.alertconfiguration.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import java.util.UUID;
import org.assertj.core.api.Java6Assertions;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class SandboxAlertConfigurationAuditDTOPrimaryKeyTest {

  @Test
  public void behavesAsBean() {
    Configuration configuration = new ConfigurationBuilder()
        .overrideFactory("alertConfigurationUUID", TestUtils::randomUUID)
        .overrideFactory("auditUUID", TestUtils::randomUUID)
        .overrideFactory("sandboxUUID", TestUtils::randomUUID).build();
    new BeanTester().testBean(SandboxAlertConfigurationAuditDTOPrimaryKey.class, configuration);
  }

  @Test
  public void parameterisedConstructor() {
    UUID sandboxUUID = TestUtils.randomUUID();
    UUID alertConfigurationUUID = TestUtils.randomUUID();
    UUID auditUUID = TestUtils.randomUUID();
    SandboxAlertConfigurationAuditDTOPrimaryKey primaryKey = new SandboxAlertConfigurationAuditDTOPrimaryKey(
        sandboxUUID, alertConfigurationUUID, auditUUID);

    assertThat(primaryKey).isNotNull();
    assertThat(primaryKey.getAuditUUID()).isNotNull();
    assertThat(primaryKey.getAuditUUID()).isEqualTo(auditUUID);
    assertThat(primaryKey.getAlertConfigurationUUID()).isNotNull();
    assertThat(primaryKey.getAlertConfigurationUUID()).isEqualTo(alertConfigurationUUID);
    assertThat(primaryKey.getSandboxUUID()).isNotNull();
    assertThat(primaryKey.getSandboxUUID()).isEqualTo(sandboxUUID);

  }

  @Test
  public void equalsTest() {

    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationid = TestUtils.randomUUID();
    UUID auditId = TestUtils.randomUUID();
    SandboxAlertConfigurationAuditDTOPrimaryKey key1 = new SandboxAlertConfigurationAuditDTOPrimaryKey();
    key1.setSandboxUUID(sandboxId);
    key1.setAlertConfigurationUUID(alertConfigurationid);
    key1.setAuditUUID(auditId);
    SandboxAlertConfigurationAuditDTOPrimaryKey key2 = null;
    Java6Assertions.assertThat(key1.equals(key2)).isFalse();
    key2 = new SandboxAlertConfigurationAuditDTOPrimaryKey();
    key2.setSandboxUUID(sandboxId);
    key2.setAlertConfigurationUUID(alertConfigurationid);
    key2.setAuditUUID(auditId);
    Java6Assertions.assertThat(key1.equals(key2)).isTrue();
    Java6Assertions.assertThat(key1.equals(key1)).isTrue();
    Java6Assertions.assertThat(key1.equals(this)).isFalse();

    key2.setAlertConfigurationUUID(TestUtils.randomUUID());
    key2.setAuditUUID(TestUtils.randomUUID());
    Java6Assertions.assertThat(key1.equals(key2)).isFalse();
    key2.setSandboxUUID(TestUtils.randomUUID());
    key2.setAuditUUID(TestUtils.randomUUID());
    Java6Assertions.assertThat(key1.equals(key2)).isFalse();
    key2.setSandboxUUID(sandboxId);
    key2.setAlertConfigurationUUID(alertConfigurationid);
    key2.setAuditUUID(TestUtils.randomUUID());
    Java6Assertions.assertThat(key1.equals(key2)).isFalse();
    key2.setSandboxUUID(sandboxId);
  }
}
