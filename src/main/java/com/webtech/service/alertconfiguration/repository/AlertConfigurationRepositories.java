package com.webtech.service.alertconfiguration.repository;

import org.springframework.stereotype.Component;

@Component
public class AlertConfigurationRepositories {

  private final SandboxAlertConfigurationRepository sandboxAlertConfigurationRepository;
  private final AlertParameterSetRepository alertParameterSetRepository;
  private final SandboxAlertConfigurationAuditRepository auditRepository;
  private final LiveAlertConfigurationRepository liveAlertConfigurationRepository;
  private final LiveAlertConfigurationAuditByAlertConfigUUIDRepository liveAlertConfigurationAuditByAlertConfigUUIDRepository;
  private final SandboxAlertConfigurationAuditByMonthRepository sandboxAuditByMonthRepository;

  public AlertConfigurationRepositories(
      AlertParameterSetRepository alertParameterSetRepository,
      SandboxAlertConfigurationRepository sandboxAlertConfigurationRepository,
      SandboxAlertConfigurationAuditRepository auditRepository,
      LiveAlertConfigurationRepository liveAlertConfigurationRepository,
      LiveAlertConfigurationAuditByAlertConfigUUIDRepository liveAlertConfigurationAuditByAlertConfigUUIDRepository,
      SandboxAlertConfigurationAuditByMonthRepository sandboxAuditByMonthRepository) {
    this.alertParameterSetRepository = alertParameterSetRepository;
    this.sandboxAlertConfigurationRepository = sandboxAlertConfigurationRepository;
    this.auditRepository = auditRepository;
    this.liveAlertConfigurationRepository = liveAlertConfigurationRepository;
    this.liveAlertConfigurationAuditByAlertConfigUUIDRepository = liveAlertConfigurationAuditByAlertConfigUUIDRepository;
    this.sandboxAuditByMonthRepository = sandboxAuditByMonthRepository;

  }

  public SandboxAlertConfigurationRepository getSandboxAlertConfigurationRepository() {
    return sandboxAlertConfigurationRepository;
  }

  public AlertParameterSetRepository getAlertParameterSetRepository() {
    return alertParameterSetRepository;
  }

  public SandboxAlertConfigurationAuditRepository getAuditRepository() {
    return auditRepository;
  }

  public LiveAlertConfigurationRepository getLiveAlertConfigurationRepository() {
    return liveAlertConfigurationRepository;
  }

  public LiveAlertConfigurationAuditByAlertConfigUUIDRepository getLiveAlertConfigurationAuditByAlertConfigUUIDRepository() {
    return liveAlertConfigurationAuditByAlertConfigUUIDRepository;
  }

  public SandboxAlertConfigurationAuditByMonthRepository getSandboxAuditByMonthRepository() {
    return sandboxAuditByMonthRepository;
  }
}
