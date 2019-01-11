package com.webtech.service.alert.dto;

import com.irisium.TestUtils;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class SandboxAlertAuditDTOTest {

  private static UUID alertId;
  private static UUID auditId;

  public static SandboxAlertAuditDTOPrimaryKey getPrimaryKey() {
    return new SandboxAlertAuditDTOPrimaryKey(alertId,
        auditId);
  }

  @Before
  public void setUp() throws Exception {
    alertId = TestUtils.randomUUID();
    auditId = TestUtils.randomUUID();
  }

  @Test
  public void behavesAsBean() {

    Configuration configuration = new ConfigurationBuilder()
        .overrideFactory("primaryKey", SandboxAlertAuditDTOTest::getPrimaryKey)
        .overrideFactory("runId", TestUtils::randomUUID)
        .overrideFactory("startTime", TestUtils::randomInstant)
        .overrideFactory("endTime", TestUtils::randomInstant)
        .overrideFactory("createdDate", TestUtils::randomInstant)
        .overrideFactory("updatedDate", TestUtils::randomInstant).build();
    new BeanTester().testBean(SandboxAlertAuditDTO.class, configuration);
  }
}
