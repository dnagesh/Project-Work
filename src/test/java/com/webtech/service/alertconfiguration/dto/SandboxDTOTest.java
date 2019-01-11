package com.webtech.service.alertconfiguration.dto;


import com.irisium.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.meanbean.test.BeanTester;
import org.meanbean.test.ConfigurationBuilder;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SandboxDTOTest {


  @Test
  public void sandboxDTOFunctionsAsBean() {
    ConfigurationBuilder configBuilder = new ConfigurationBuilder()
        .overrideFactory("createdWhen", () -> TestUtils.randomInstant())
        .overrideFactory("uuid", () -> TestUtils.randomUUID());
    new BeanTester().testBean(SandboxDTO.class, configBuilder.build());

  }
}
