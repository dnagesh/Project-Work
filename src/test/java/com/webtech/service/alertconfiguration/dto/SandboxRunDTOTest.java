package com.webtech.service.alertconfiguration.dto;


import com.irisium.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meanbean.test.BeanTester;
import org.meanbean.test.ConfigurationBuilder;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SandboxRunDTOTest {


  @Test
  public void sandboxRunDTOFunctionsAsBean() {
    ConfigurationBuilder configBuilder = new ConfigurationBuilder()
        .overrideFactory("startTime", () -> TestUtils.randomInstant())
        .overrideFactory("endTime", () -> TestUtils.randomInstant())
        .overrideFactory("dataFrom", () -> TestUtils.randomInstant())
        .overrideFactory("dataTo", () -> TestUtils.randomInstant())
        .overrideFactory("uuid", () -> TestUtils.randomUUID())
        .overrideFactory("sandboxUUID", () -> TestUtils.randomUUID())
        .overrideFactory("primaryKey", SandboxRunDTOPrimaryKey::new);
    new BeanTester().testBean(SandboxRunDTO.class, configBuilder.build());

  }

}

