package com.webtech.service.alert.dto;

import com.irisium.TestUtils;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class SandboxAlertDTOTest {


  private static UUID runId;
  private static UUID auditId;

  public static SandboxAlertDTOPrimaryKey getPrimaryKey() {
    return new SandboxAlertDTOPrimaryKey(runId,
        auditId);
  }

  @Before
  public void setUp() throws Exception {
    runId = TestUtils.randomUUID();
    auditId = TestUtils.randomUUID();
  }

  @Test
  public void behavesAsBean() {
    Configuration configuration = new ConfigurationBuilder()
        .overrideFactory("primaryKey", SandboxAlertDTOTest::getPrimaryKey)
        .overrideFactory("startTime", TestUtils::randomInstant)
        .overrideFactory("endTime", TestUtils::randomInstant)
        .overrideFactory("createdDate", TestUtils::randomInstant)
        .overrideFactory("updatedDate", TestUtils::randomInstant).build();
    new BeanTester().testBean(SandboxAlertDTO.class, configuration);
  }
}
