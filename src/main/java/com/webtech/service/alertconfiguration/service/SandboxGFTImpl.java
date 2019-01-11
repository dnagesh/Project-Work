package com.webtech.service.alertconfiguration.service;

import com.webtech.service.alertconfiguration.SandboxesApiDelegateImpl;
import com.webtech.service.alertconfiguration.exception.SandboxRunNotFoundException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SandboxGFTImpl {

  private static final Logger LOGGER = LoggerFactory.getLogger(SandboxesApiDelegateImpl.class);
  private final SandboxRunService sandboxRunService;

  public SandboxGFTImpl(SandboxRunService sandboxRunService) {
    this.sandboxRunService = sandboxRunService;
  }

  public synchronized void callGFTAPI(String sandboxId, String runId) {

    try {
      TimeUnit.SECONDS.sleep(1);
      sandboxRunService.updateSandboxRunWhenFinished(sandboxId, runId);
    } catch (InterruptedException ex) {
      LOGGER.error("Interrupted!", ex);
      Thread.currentThread().interrupt();
    } catch (SandboxRunNotFoundException ex) {
      LOGGER.error(ex.getMessage());
    }
  }

}
