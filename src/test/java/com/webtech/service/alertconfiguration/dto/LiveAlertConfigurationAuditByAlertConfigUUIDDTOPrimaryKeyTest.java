package com.webtech.service.alertconfiguration.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKeyTest {

  @Test
  public void behavesAsBean() {
    Configuration configuration = new ConfigurationBuilder()
        .overrideFactory("alertConfigUUID", TestUtils::randomUUID)
        .overrideFactory("auditTimestamp", TestUtils::randomInstant).build();
    new BeanTester()
        .testBean(LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey.class, configuration);
  }

  @Test
  public void parameterisedConstructorTest() {
    Instant auditTimestamp = TestUtils.randomInstant();
    UUID alertConfigUUID = TestUtils.randomUUID();

    LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey primaryKey = new LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey(
        alertConfigUUID, auditTimestamp);

    assertThat(primaryKey).isNotNull();
    assertThat(primaryKey.getAlertConfigUUID()).isEqualTo(alertConfigUUID);
    assertThat(primaryKey.getAuditTimestamp()).isEqualTo(auditTimestamp);

  }

}
