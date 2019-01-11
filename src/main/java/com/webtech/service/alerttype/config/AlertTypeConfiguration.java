package com.webtech.service.alerttype.config;

import java.io.IOException;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;

@Profile("!integrationTest")
@Configuration
public class AlertTypeConfiguration extends BaseAlertTypeConfiguration {

  @Value("classpath:baseAlertTypes.json")
  private Resource baseAlertTypesResource;

  @Value("classpath:deploymentAlertTypes.json")
  private Resource deploymentAlertTypesResource;

  @PostConstruct
  private void init() throws IOException {
    super.init(baseAlertTypesResource, deploymentAlertTypesResource);
  }
}
