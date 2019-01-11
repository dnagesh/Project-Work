package com.webtech.service.alertconfiguration.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.irisium.TestUtils;
import com.webtech.service.alertconfiguration.repository.SandboxRunRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SandboxGFTImplTest {

  @Mock
  private SandboxRunService mockService;
  @Mock
  private SandboxRunRepository mockRepository;

  private SandboxGFTImpl sandboxGFT;

  @Test
  public void shouldCallUpdateSandboxRunWhenFinishedForAnyGivenSandboxIdForCallGFTAPI()
      throws Throwable {

    String sandboxId = TestUtils.randomUUID().toString();
    String sandboxRunId = TestUtils.randomUUID().toString();
    sandboxGFT = new SandboxGFTImpl(mockService);
    sandboxGFT.callGFTAPI(sandboxId, sandboxRunId);
    verify(mockService, times(1)).updateSandboxRunWhenFinished(sandboxId, sandboxRunId);
  }

  @Test
  public void shouldThrowSandboxRunNotFoundExceptionIfNoSandboxRunFoundForCallGFTAPI()
      throws Throwable {
    String sandboxId = TestUtils.randomUUID().toString();
    String sandboxRunId = TestUtils.randomUUID().toString();
    SandboxRunService sandboxRunService = new SandboxRunService(mockRepository, null,
        null, null);
    sandboxGFT = new SandboxGFTImpl(sandboxRunService);
    sandboxGFT.callGFTAPI(sandboxId, sandboxId);
    verify(mockService, times(0)).updateSandboxRunWhenFinished(sandboxId, sandboxRunId);
  }

  @Test
  public void shouldThrowInterruptedExceptionForCallGFTAPI()
      throws Throwable {
    String sandboxId = TestUtils.randomUUID().toString();
    String sandboxRunId = TestUtils.randomUUID().toString();
    SandboxRunService sandboxRunService = new SandboxRunService(mockRepository, null,
        null, null);
    sandboxGFT = new SandboxGFTImpl(sandboxRunService);
    Runnable task = () -> sandboxGFT.callGFTAPI(sandboxId, sandboxId);

    // start the thread
    Thread t = new Thread(task);
    t.start();
    t.interrupt();
    verify(mockService, times(0)).updateSandboxRunWhenFinished(sandboxId, sandboxRunId);
  }

}
