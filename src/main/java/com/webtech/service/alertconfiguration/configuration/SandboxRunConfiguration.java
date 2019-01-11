package com.webtech.service.alertconfiguration.configuration;

import com.webtech.service.alertconfiguration.service.SandboxRunService;
import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
public class SandboxRunConfiguration {

  private final SandboxRunService sandboxRunService;

  SandboxRunConfiguration(SandboxRunService sandboxRunService) {
    this.sandboxRunService = sandboxRunService;
  }


  @Async("threadPoolTaskExecutor")
  public void callGFTAPI() throws InterruptedException {
    Thread.sleep(1000);
  }

  @Bean(name = "threadPoolTaskExecutor")
  public Executor threadPoolTaskExecutor() {
    return new ThreadPoolTaskExecutor();
  }
}
