package com.webtech.service.alertconfiguration;


import com.webtech.service.common.configuration.BaseServiceConfiguration;
import com.webtech.service.guinotification.config.BaseGuiNotificationServiceConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan("com.irisium.service")
@Import({
    BaseServiceConfiguration.class,
    BaseGuiNotificationServiceConfiguration.class
})
public class AlertConfigurationServiceApplication {

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(
        AlertConfigurationServiceApplication.class);
    application.run(args);
  }
}
