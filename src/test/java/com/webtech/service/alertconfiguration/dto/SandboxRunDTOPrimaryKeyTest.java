package com.webtech.service.alertconfiguration.dto;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.irisium.TestUtils;
import java.util.UUID;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class SandboxRunDTOPrimaryKeyTest {

  @Test
  public void behavesAsBean() {
    Configuration configuration = new ConfigurationBuilder()
        .overrideFactory("runUUID", TestUtils::randomUUID)
        .overrideFactory("sandboxUUID", TestUtils::randomUUID).build();
    new BeanTester().testBean(SandboxRunDTOPrimaryKey.class, configuration);
  }

  @Test
  public void equalsTest() {

    UUID sandboxId = TestUtils.randomUUID();
    UUID runId = TestUtils.randomUUID();
    SandboxRunDTOPrimaryKey key1 = new SandboxRunDTOPrimaryKey();
    key1.setSandboxUUID(sandboxId);
    key1.setRunUUID(runId);
    SandboxRunDTOPrimaryKey key2 = null;
    assertThat(key1.equals(key2)).isFalse();
    key2 = new SandboxRunDTOPrimaryKey();
    key2.setSandboxUUID(sandboxId);
    key2.setRunUUID(runId);
    assertThat(key1.equals(key2)).isTrue();
    assertThat(key1.equals(key1)).isTrue();
    assertThat(key1.equals(this)).isFalse();

    key2.setRunUUID(TestUtils.randomUUID());
    assertThat(key1.equals(key2)).isFalse();
    key2.setSandboxUUID(TestUtils.randomUUID());
    assertThat(key1.equals(key2)).isFalse();

  }
}
