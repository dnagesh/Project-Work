package com.webtech.service.alertconfiguration.dto;

import com.irisium.TestUtils;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class LiveAlertConfigurationDTOTest {

  @Test
  public void behavesAsBean() {
    Configuration configuration = new ConfigurationBuilder()
        .overrideFactory("uuid", TestUtils::randomUUID)
        .overrideFactory("createdWhen", TestUtils::randomInstant)
        .overrideFactory("updatedWhen", TestUtils::randomInstant).build();
    new BeanTester().testBean(LiveAlertConfigurationDTO.class, configuration);
  }

}
