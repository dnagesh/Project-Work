package com.webtech.service.alertconfiguration.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class LiveAlertConfigurationAuditByMonthDTOPrimaryKeyTest {

  @Test
  public void behavesAsBean() {
    Configuration configuration = new ConfigurationBuilder()
        .overrideFactory("whenMonth",
            TestUtils::randomLocalDate)
        .overrideFactory("auditUUID", TestUtils::randomUUID).build();
    new BeanTester().testBean(LiveAlertConfigurationAuditByMonthDTOPrimaryKey.class, configuration);
  }

  @Test
  public void parameterisedConstructorTest() {
    LocalDate whenMonth = TestUtils.randomLocalDate();
    UUID auditUUID = TestUtils.randomUUID();
    LiveAlertConfigurationAuditByMonthDTOPrimaryKey primaryKey = new LiveAlertConfigurationAuditByMonthDTOPrimaryKey(
        whenMonth, auditUUID);
    assertThat(primaryKey).isNotNull();
    assertThat(primaryKey.getAuditUUID()).isNotNull();
    assertThat(primaryKey.getAuditUUID()).isEqualTo(auditUUID);
    assertThat(primaryKey.getWhenMonth()).isNotNull();
    assertThat(primaryKey.getWhenMonth()).isEqualTo(whenMonth);
  }
}
