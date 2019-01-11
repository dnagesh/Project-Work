package com.webtech.service.alertconfiguration.dto;

import com.irisium.TestUtils;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class SandboxAlertConfigurationAuditDTOTest {

  @Test
  public void behavesAsBean() {
    Configuration configuration = new ConfigurationBuilder()
        .overrideFactory("auditUUID", TestUtils::randomUUID)
        .overrideFactory("sandboxUUID", TestUtils::randomUUID)
        .overrideFactory("uuid", TestUtils::randomUUID)
        .overrideFactory("liveConfigUUID", TestUtils::randomUUID)
        .overrideFactory("createdWhen", TestUtils::randomInstant)
        .overrideFactory("updatedWhen", TestUtils::randomInstant)
        .overrideFactory("primaryKey", SandboxAlertConfigurationAuditDTOPrimaryKey::new).build();
    new BeanTester().testBean(SandboxAlertConfigurationAuditDTO.class, configuration);
  }
}
