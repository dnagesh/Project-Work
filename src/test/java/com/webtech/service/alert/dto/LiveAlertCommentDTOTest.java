package com.webtech.service.alert.dto;

import com.irisium.TestUtils;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class LiveAlertCommentDTOTest {

  @Test
  public void behavesAsBean() {
    Configuration configuration = new ConfigurationBuilder()
        .overrideFactory("alertId", TestUtils::randomUUID)
        .overrideFactory("commentId", TestUtils::randomUUID)
        .overrideFactory("creationTime", TestUtils::randomInstant)
        .build();
    new BeanTester().testBean(LiveAlertCommentDTO.class, configuration);
  }
}
