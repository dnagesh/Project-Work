package com.webtech.service.alertconfiguration.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import java.util.UUID;
import org.assertj.core.api.Java6Assertions;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class SandboxAlertConfigurationDTOPrimaryKeyTest {

  @Test
  public void behavesAsBean() {
    Configuration configuration = new ConfigurationBuilder()
        .overrideFactory("alertConfigurationUUID", TestUtils::randomUUID)
        .overrideFactory("sandboxUUID", TestUtils::randomUUID).build();
    new BeanTester().testBean(SandboxAlertConfigurationDTOPrimaryKey.class, configuration);
  }

  @Test
  public void parameterisedConstructorTest() {
    UUID alertConfigurationUUID = TestUtils.randomUUID();
    UUID sandboxUUID = TestUtils.randomUUID();
    SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey(
        sandboxUUID, alertConfigurationUUID);

    assertThat(primaryKey).isNotNull();
    assertThat(primaryKey.getAlertConfigurationUUID()).isEqualTo(alertConfigurationUUID);
    assertThat(primaryKey.getSandboxUUID()).isEqualTo(sandboxUUID);
  }

  @Test
  public void equalsTest() {

    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationid = TestUtils.randomUUID();
    SandboxAlertConfigurationDTOPrimaryKey key1 = new SandboxAlertConfigurationDTOPrimaryKey();
    key1.setSandboxUUID(sandboxId);
    key1.setAlertConfigurationUUID(alertConfigurationid);
    SandboxAlertConfigurationDTOPrimaryKey key2 = null;
    Java6Assertions.assertThat(key1.equals(key2)).isFalse();
    key2 = new SandboxAlertConfigurationDTOPrimaryKey();
    key2.setSandboxUUID(sandboxId);
    key2.setAlertConfigurationUUID(alertConfigurationid);
    Java6Assertions.assertThat(key1.equals(key2)).isTrue();
    Java6Assertions.assertThat(key1.equals(key1)).isTrue();
    Java6Assertions.assertThat(key1.equals(this)).isFalse();

    key2.setAlertConfigurationUUID(TestUtils.randomUUID());
    Java6Assertions.assertThat(key1.equals(key2)).isFalse();
    key2.setSandboxUUID(TestUtils.randomUUID());
    Java6Assertions.assertThat(key1.equals(key2)).isFalse();

  }
}
