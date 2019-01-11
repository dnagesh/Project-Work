package com.webtech.service.alert.dto;

import com.irisium.TestUtils;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class LiveAlertAuditDTOPrimaryKeyTest {

  @Test
  public void behavesAsBean() {
    Configuration configuration = new ConfigurationBuilder()
        .overrideFactory("alertId", TestUtils::randomUUID)
        .overrideFactory("auditId", TestUtils::randomUUID).build();
    new BeanTester().testBean(LiveAlertAuditDTOPrimaryKey.class, configuration);
  }
}
