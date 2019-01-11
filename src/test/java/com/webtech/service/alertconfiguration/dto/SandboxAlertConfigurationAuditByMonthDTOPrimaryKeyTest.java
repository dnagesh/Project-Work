package com.webtech.service.alertconfiguration.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class SandboxAlertConfigurationAuditByMonthDTOPrimaryKeyTest {

  @Test
  public void behavesAsBean() {
    Configuration configuration = new ConfigurationBuilder()
        .overrideFactory("whenMonth",
            TestUtils::randomLocalDate)
        .overrideFactory("sandboxUUID", TestUtils::randomUUID)
        .overrideFactory("auditUUID", TestUtils::randomUUID).build();
    new BeanTester()
        .testBean(SandboxAlertConfigurationAuditByMonthDTOPrimaryKey.class, configuration);
  }

  @Test
  public void parameterisedConstructorTest() {
    LocalDate whenMonth = TestUtils.randomLocalDate();
    UUID sandboxUUID = TestUtils.randomUUID();
    UUID auditUUID = TestUtils.randomUUID();
    SandboxAlertConfigurationAuditByMonthDTOPrimaryKey primaryKey = new SandboxAlertConfigurationAuditByMonthDTOPrimaryKey(
        whenMonth, sandboxUUID, auditUUID);
    assertThat(primaryKey).isNotNull();
    assertThat(primaryKey.getSandboxUUID()).isNotNull();
    assertThat(primaryKey.getSandboxUUID()).isEqualTo(sandboxUUID);
    assertThat(primaryKey.getWhenMonth()).isNotNull();
    assertThat(primaryKey.getWhenMonth()).isEqualTo(whenMonth);
  }
}
