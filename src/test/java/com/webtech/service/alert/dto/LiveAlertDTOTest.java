package com.webtech.service.alert.dto;

import com.irisium.TestUtils;
import java.util.Random;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class LiveAlertDTOTest {

  private Random random = new Random();

  @Test
  public void behavesAsBean() {
    Configuration configuration = new ConfigurationBuilder()
        .overrideFactory("alertId", TestUtils::randomUUID)
        .overrideFactory("runId", TestUtils::randomUUID)
        .overrideFactory("sandboxAlertId", TestUtils::randomUUID)
        .overrideFactory("createdDate", TestUtils::randomInstant)
        .overrideFactory("updatedDate", TestUtils::randomInstant)
        .overrideFactory("startTime", TestUtils::randomInstant)
        .overrideFactory("endTime", TestUtils::randomInstant).build();
    new BeanTester().testBean(LiveAlertDTO.class, configuration);
  }

}
