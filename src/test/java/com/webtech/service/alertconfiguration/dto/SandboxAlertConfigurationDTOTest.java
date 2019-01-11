package com.webtech.service.alertconfiguration.dto;

import com.irisium.TestUtils;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class SandboxAlertConfigurationDTOTest {

  @Test
  public void behavesAsBean() {
    Configuration configuration = new ConfigurationBuilder()
        .overrideFactory("createdWhen", TestUtils::randomInstant)
        .overrideFactory("updatedWhen", TestUtils::randomInstant)
        .overrideFactory("primaryKey", SandboxAlertConfigurationDTOPrimaryKey::new)
        .overrideFactory("liveConfigUUID", TestUtils::randomUUID)
        .build();
    new BeanTester().testBean(SandboxAlertConfigurationDTO.class, configuration);
  }

}
