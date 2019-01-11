package com.webtech.service.alertconfiguration;

import com.irisium.service.alertconfiguration.api.SandboxesApiDelegate;
import com.webtech.service.alertconfiguration.exception.AlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.exception.AuditNotFoundException;
import com.webtech.service.alertconfiguration.exception.IllegalParameterException;
import com.webtech.service.alertconfiguration.exception.SandboxAlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.exception.SandboxNotFoundException;
import com.irisium.service.alertconfiguration.model.AlertConfigurationTagsMap;
import com.irisium.service.alertconfiguration.model.CloneSandboxAlertConfigRequest;
import com.irisium.service.alertconfiguration.model.CreateSandboxRequest;
import com.irisium.service.alertconfiguration.model.CreateSandboxRunRequest;
import com.irisium.service.alertconfiguration.model.CreateUpdateSandboxAlertConfigRequest;
import com.irisium.service.alertconfiguration.model.EntityLinkRequest;
import com.irisium.service.alertconfiguration.model.EntityRelationship;
import com.irisium.service.alertconfiguration.model.EntityRelationshipAudit;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration;
import com.irisium.service.alertconfiguration.model.Sandbox;
import com.irisium.service.alertconfiguration.model.SandboxAlertConfiguration;
import com.irisium.service.alertconfiguration.model.SandboxAlertConfigurationAudit;
import com.irisium.service.alertconfiguration.model.SandboxResetOptions;
import com.irisium.service.alertconfiguration.model.SandboxRun;
import com.irisium.service.alertconfiguration.model.UpdateStatus;
import com.webtech.service.alertconfiguration.service.AlertConfigurationRelationshipService;
import com.webtech.service.alertconfiguration.service.LiveAlertConfigurationQueryService;
import com.webtech.service.alertconfiguration.service.SandboxAlertConfigurationService;
import com.webtech.service.alertconfiguration.service.SandboxGFTImpl;
import com.webtech.service.alertconfiguration.service.SandboxRunService;
import com.webtech.service.alertconfiguration.service.SandboxService;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.webtech.service.common.security.PrincipalProvider;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.guinotification.GuiNotificationMessage;
import com.webtech.service.guinotification.GuiNotificationPublisher;
import com.webtech.service.guinotification.GuiNotificationTypeEnum;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.webtech.service.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class SandboxesApiDelegateImpl implements SandboxesApiDelegate {

  public static final String UNABLE_TO_FIND_THE_PRINCIPAL = "Unable to find the Principal";
  private final SandboxService sandboxService;
  private final SandboxRunService sandboxRunService;
  private final PrincipalProvider principalProvider;
  private final SandboxAlertConfigurationService sandboxAlertConfigurationService;
  private final LiveAlertConfigurationQueryService liveAlertConfigurationQueryService;
  private final SandboxGFTImpl sandboxGFT;
  private final AlertConfigurationRelationshipService alertConfigurationRelationshipService;
  @Autowired
  private GuiNotificationPublisher guiNotificationPublisher;

  public SandboxesApiDelegateImpl(
      SandboxService sandboxService,
      SandboxRunService sandboxRunService,
      PrincipalProvider principalProvider,
      SandboxAlertConfigurationService sandboxAlertConfigurationService,
      LiveAlertConfigurationQueryService liveAlertConfigurationQueryService,
      SandboxGFTImpl sandboxGFT,
      AlertConfigurationRelationshipService alertConfigurationRelationshipService) {
    this.sandboxService = sandboxService;
    this.sandboxRunService = sandboxRunService;
    this.principalProvider = principalProvider;
    this.sandboxAlertConfigurationService = sandboxAlertConfigurationService;
    this.liveAlertConfigurationQueryService = liveAlertConfigurationQueryService;
    this.sandboxGFT = sandboxGFT;
    this.alertConfigurationRelationshipService = alertConfigurationRelationshipService;

  }


  @Override
  public ResponseEntity<List<Sandbox>> getAllSandboxes() {
    List<Sandbox> sandboxes = sandboxService.getAllSandboxes();
    return ResponseEntity.ok(sandboxes);
  }

  @Override
  public ResponseEntity<Sandbox> getSandboxById(String sandboxId) {
    Optional<Sandbox> sandbox = sandboxService.getSandboxById(sandboxId);
    if (sandbox.isPresent()) {
      return ResponseEntity.ok(sandbox.get());
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Override
  public ResponseEntity<Sandbox> createSandbox(CreateSandboxRequest createSandboxRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    Sandbox sandbox = null;
    if (principal.isPresent()) {
      try {
        sandbox = sandboxService.createSandbox(createSandboxRequest, principal.get());
        sendGuiNotification(sandbox.getId(),
            GuiNotificationTypeEnum.SANDBOX,
            sandbox.getCreatedWhen());
      } catch (AlertConfigurationNotFoundException e) {
        return ResponseEntity.badRequest().build();
      }
      return ResponseEntity.ok(sandbox);
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), UNABLE_TO_FIND_THE_PRINCIPAL).build();
  }

  @Override
  public ResponseEntity<Void> deleteSandboxById(String sandboxId) {
    try {
      sandboxService.deleteSandboxById(sandboxId);
      sendGuiNotification(sandboxId,
          GuiNotificationTypeEnum.SANDBOX,
          Instant.now());
      return ResponseEntity.noContent().build();
    } catch (SandboxNotFoundException ex) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public ResponseEntity<SandboxRun> createSandboxRun(String sandboxId,
      CreateSandboxRunRequest createSandboxRunRequest) {

    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), UNABLE_TO_FIND_THE_PRINCIPAL).build();
    }

    Optional<Sandbox> sandbox = sandboxService.getSandboxById(sandboxId);
    if (sandbox.isPresent()) {
      SandboxRun sandboxRun = sandboxRunService
          .createSandboxRun(sandboxId, createSandboxRunRequest, principal.get());
      CompletableFuture
          .runAsync(() -> sandboxGFT.callGFTAPI(sandboxRun.getSandboxId(), sandboxRun.getId()));
      sendGuiNotification(sandboxId,
          GuiNotificationTypeEnum.SANDBOX,
          sandboxRun.getRunStartTime());
      sendGuiNotification(sandboxRun.getId(),
          GuiNotificationTypeEnum.SANDBOXRUN,
          sandboxRun.getRunStartTime());
      return ResponseEntity.ok(sandboxRun);
    } else {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

  }

  @Override
  public ResponseEntity<List<SandboxRun>> getAllRunsBySandboxId(String sandboxId) {
    List<SandboxRun> sandboxRuns = sandboxRunService.getAllRunsBySandboxId(sandboxId);
    return ResponseEntity.ok(sandboxRuns);
  }

  @Override
  public ResponseEntity<SandboxRun> getSandboxRunById(String sandboxId, String runId) {
    Optional<SandboxRun> sandboxRun = sandboxRunService.getSandboxRunByRunId(sandboxId, runId);
    if (sandboxRun.isPresent()) {
      return ResponseEntity.ok(sandboxRun.get());
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Override
  public ResponseEntity<SandboxAlertConfiguration> addSandboxAlertConfiguration(String sandboxId,
      CreateUpdateSandboxAlertConfigRequest createSandboxAlertConfigRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), UNABLE_TO_FIND_THE_PRINCIPAL).build();
    }

    Optional<Sandbox> sandbox = sandboxService.getSandboxById(sandboxId);
    if (sandbox.isPresent()) {
      try {

        SandboxAlertConfiguration sandboxAlertConfiguration = sandboxAlertConfigurationService
            .addAlertConfigToSandbox(sandbox.get().getId(), createSandboxAlertConfigRequest,
                principal.get());
        sendGuiNotification(sandboxAlertConfiguration.getAlertConfigurationUUID(),
            GuiNotificationTypeEnum.SANDBOXALERTCONFIGURATION,
            sandboxAlertConfiguration.getWhen());
        sendGuiNotification(sandbox.get().getId(),
            GuiNotificationTypeEnum.SANDBOX,
            sandboxAlertConfiguration.getWhen());
        return ResponseEntity.ok(sandboxAlertConfiguration);
      } catch (NoSuchAlgorithmException e) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), "Unable to hash parameter set").build();
      } catch (IOException | EntityNotFoundException e) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
      } catch (IllegalParameterException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
      }
    } else {

      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @Override
  public ResponseEntity<SandboxAlertConfiguration> updateSandboxAlertConfiguration(String sandboxId,
      String alertConfigurationId,
      CreateUpdateSandboxAlertConfigRequest updateSandboxAlertConfigRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), UNABLE_TO_FIND_THE_PRINCIPAL).build();
    }

    Optional<Sandbox> sandbox = sandboxService.getSandboxById(sandboxId);
    if (sandbox.isPresent()) {
      try {
        SandboxAlertConfiguration savedConfig = sandboxAlertConfigurationService
            .createUpdateSandboxAlertConfiguration(sandbox.get().getId(), alertConfigurationId,
                updateSandboxAlertConfigRequest, principal.get());
        sendGuiNotification(sandbox.get().getId(),
            GuiNotificationTypeEnum.SANDBOX,
            savedConfig.getUpdatedWhen());
        sendGuiNotification(savedConfig.getAlertConfigurationUUID(),
            GuiNotificationTypeEnum.SANDBOXALERTCONFIGURATION,
            savedConfig.getUpdatedWhen());
        return ResponseEntity.ok(savedConfig);

      } catch (SandboxAlertConfigurationNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(),
                "Failed to find sandbox alert configuration").build();
      } catch (NoSuchAlgorithmException e) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), "Unable to hash parameter set").build();
      } catch (IOException | EntityNotFoundException e) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
      } catch (IllegalParameterException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
      }
    } else {

      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), "Sandbox not found").build();
    }
  }

  @Override
  public ResponseEntity<SandboxAlertConfigurationAudit> getOriginalSandboxAlertConfiguration(
      String sandboxId, String alertConfigurationId) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), UNABLE_TO_FIND_THE_PRINCIPAL).build();
    }

    Optional<Sandbox> sandbox = sandboxService.getSandboxById(sandboxId);
    if (sandbox.isPresent()) {
      Optional<SandboxAlertConfigurationAudit> audit = sandboxAlertConfigurationService
          .getOriginalSandboxAlertConfiguration(sandbox.get().getId(), alertConfigurationId);
      return audit.isPresent() ? ResponseEntity.ok(audit.get()) : ResponseEntity.notFound().build();

    } else {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public ResponseEntity<LiveAlertConfiguration> getLiveConfigForSandboxAlertConfiguration(
      String sandboxId, String alertConfigurationId) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), UNABLE_TO_FIND_THE_PRINCIPAL).build();
    }

    Optional<SandboxAlertConfiguration> sandboxAlertConfiguration = sandboxAlertConfigurationService
        .getSandboxAlertConfiguration(sandboxId, alertConfigurationId);
    if (sandboxAlertConfiguration.isPresent()) {
      if (sandboxAlertConfiguration.get().getLiveConfigUUID() != null) {
        Optional<LiveAlertConfiguration> liveAlertConfiguration = liveAlertConfigurationQueryService
            .getLiveAlertConfigurationById(sandboxAlertConfiguration.get().getLiveConfigUUID());

        return liveAlertConfiguration.isPresent() ? ResponseEntity.ok(liveAlertConfiguration.get())
            : ResponseEntity.notFound().build();
      } else {
        return ResponseEntity.notFound().build();
      }
    } else {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public ResponseEntity<List<SandboxAlertConfiguration>> getSandboxAlertConfigurations(
      String sandboxId) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), UNABLE_TO_FIND_THE_PRINCIPAL).build();
    }
    Optional<Sandbox> sandbox = sandboxService.getSandboxById(sandboxId);
    if (sandbox.isPresent()) {
      List<SandboxAlertConfiguration> sandboxAlertConfigurations = sandboxAlertConfigurationService
          .getSandboxAlertConfigurations(sandboxId);
      if (CollectionUtils.isEmpty(sandboxAlertConfigurations)) {
        return ResponseEntity.noContent().build();
      } else {
        return ResponseEntity.ok(sandboxAlertConfigurations);
      }
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

  }

  @Override
  public ResponseEntity<EntityRelationship> linkTagToSandboxAlertConfiguration(String sandboxId,
      String alertConfigurationId,
      EntityLinkRequest tagLinkRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      EntityRelationship relationship = alertConfigurationRelationshipService
          .createRelationship(alertConfigurationId, tagLinkRequest,
              EntityType.SANDBOXALERTCONFIGURATION, EntityType.TAG,
              principal.get(), sandboxId);
      sendGuiNotification(relationship.getFromId(),
          GuiNotificationTypeEnum.SANDBOXALERTCONFIGURATION_REL_TAG,
          relationship.getWhen());
      return ResponseEntity.ok(relationship);
    } catch (AlertConfigurationNotFoundException e) {
      return ResponseEntity.notFound()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }


  @Override
  public ResponseEntity<Void> delinkTagFromSandboxAlertConfiguration(String sandboxId,
      String alertConfigurationId,
      String tagId) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      EntityRelationship relationship = alertConfigurationRelationshipService
          .deleteRelationship(alertConfigurationId, tagId, EntityType.SANDBOXALERTCONFIGURATION,
              EntityType.TAG, principal.get(), sandboxId);
      sendGuiNotification(relationship.getFromId(),
          GuiNotificationTypeEnum.SANDBOXALERTCONFIGURATION_REL_TAG,
          relationship.getWhen());
      return ResponseEntity.noContent().build();
    } catch (AlertConfigurationNotFoundException e) {
      return ResponseEntity.badRequest()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }


  @Override
  public ResponseEntity<List<String>> getTagsBySandboxAlertConfigurationId(String sandboxId,
      String alertConfigurationId) {
    List<String> tags = alertConfigurationRelationshipService
        .getRelationshipsByAlertConfigurationId(alertConfigurationId, EntityType.TAG);
    return ResponseEntity.ok(tags);
  }

  @Override
  public ResponseEntity<List<EntityRelationshipAudit>> getSandboxAlertConfigurationTagRelationshipAudit(
      String sandboxId, String alertConfigurationId,
      String tagId) {
    return getRelationshipAudit(alertConfigurationId, tagId, sandboxId);
  }

  private ResponseEntity<List<EntityRelationshipAudit>> getRelationshipAudit(
      String alertConfigurationId,
      String entityId, String sandboxId) {
    try {
      List<EntityRelationshipAudit> auditHistory = alertConfigurationRelationshipService
          .getRelationshipAudit(alertConfigurationId, entityId, sandboxId);
      if (CollectionUtils.isEmpty(auditHistory)) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(auditHistory);
    } catch (AlertConfigurationNotFoundException e) {
      return ResponseEntity.badRequest()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<SandboxAlertConfiguration> updateSandboxAlertConfigurationStatus(
      String sandboxId, String alertConfigurationUUID, UpdateStatus status) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    Optional<Sandbox> sandbox = sandboxService.getSandboxById(sandboxId);
    if (sandbox.isPresent()) {
      try {
        SandboxAlertConfiguration alertConfiguration = sandboxAlertConfigurationService
            .updateStatus(sandboxId, alertConfigurationUUID, status, principal.get());
        sendGuiNotification(sandbox.get().getId(),
            GuiNotificationTypeEnum.SANDBOX,
            alertConfiguration.getUpdatedWhen());
        sendGuiNotification(alertConfigurationUUID,
            GuiNotificationTypeEnum.SANDBOXALERTCONFIGURATION,
            alertConfiguration.getUpdatedWhen());
        return ResponseEntity.ok(alertConfiguration);
      } catch (SandboxAlertConfigurationNotFoundException e) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
    } else {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }


  @Override
  public ResponseEntity<Sandbox> resetSandbox(String sandboxId,
      SandboxResetOptions resetSandboxRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    Sandbox sandbox = null;
    if (principal.isPresent()) {
      try {
        sandbox = sandboxService.resetSandbox(sandboxId, resetSandboxRequest, principal.get());
        sendGuiNotification(sandbox.getId(),
            GuiNotificationTypeEnum.SANDBOX,
            sandbox.getCreatedWhen());
      } catch (SandboxNotFoundException e) {
        return ResponseEntity.notFound().build();
      }

      return ResponseEntity.ok(sandbox);
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), UNABLE_TO_FIND_THE_PRINCIPAL).build();
  }

  @Override
  public ResponseEntity<List<SandboxAlertConfigurationAudit>> getAllSandboxAlertConfigurationsAudit(
      String sandboxId, Integer numberOfRecords, Integer maxAge) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    Optional<Sandbox> sandbox = sandboxService.getSandboxById(sandboxId);
    if (sandbox.isPresent()) {
      List<SandboxAlertConfigurationAudit> alertConfigurationAudits = sandboxAlertConfigurationService
          .getAllSandboxAlertConfigurationsAudit(sandboxId, numberOfRecords, maxAge);
      if (CollectionUtils.isEmpty(alertConfigurationAudits)) {
        return ResponseEntity.noContent().build();
      }
      return ResponseEntity.ok(alertConfigurationAudits);
    } else {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public ResponseEntity<List<SandboxAlertConfigurationAudit>> getAuditHistoryForSandboxAlertConfiguration(
      String sandboxId, String alertConfigurationUUID) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    Optional<Sandbox> sandbox = sandboxService.getSandboxById(sandboxId);
    if (sandbox.isPresent()) {
      try {
        List<SandboxAlertConfigurationAudit> alertConfigurationAudits = sandboxAlertConfigurationService
            .getAuditHistoryForSandboxAlertConfiguration(sandboxId, alertConfigurationUUID);
        if (CollectionUtils.isEmpty(alertConfigurationAudits)) {
          return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(alertConfigurationAudits);
      } catch (SandboxAlertConfigurationNotFoundException e) {
        return ResponseEntity.notFound().build();
      }
    } else {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public ResponseEntity<SandboxAlertConfigurationAudit> getAuditDetailsForSandboxAlertConfiguration(
      String sandboxId, String alertConfigurationUUID, String auditUUID) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    Optional<Sandbox> sandbox = sandboxService.getSandboxById(sandboxId);
    if (sandbox.isPresent()) {
      try {
        SandboxAlertConfigurationAudit audit = sandboxAlertConfigurationService
            .getSandboxAlertConfigurationAuditById(sandboxId, alertConfigurationUUID, auditUUID);
        return ResponseEntity.ok(audit);
      } catch (AuditNotFoundException e) {
        return ResponseEntity.notFound().build();
      } catch (SandboxAlertConfigurationNotFoundException e) {
        return ResponseEntity.badRequest().build();
      }
    } else {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public ResponseEntity<AlertConfigurationTagsMap> getBulkTagRequestForSandboxAlertConfigurationIds(
      String sandboxId, List<String> alertConfigurationIds) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    Optional<Sandbox> sandbox = sandboxService.getSandboxById(sandboxId);
    if (sandbox.isPresent()) {
      AlertConfigurationTagsMap tagsMap = new AlertConfigurationTagsMap();
      alertConfigurationIds.forEach(alertConfigurationId -> {
        List<String> tags = alertConfigurationRelationshipService
            .getRelationshipsByAlertConfigurationId(alertConfigurationId, EntityType.TAG);
        tagsMap.put(alertConfigurationId, tags);
      });
      return ResponseEntity.ok(tagsMap);
    } else {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public ResponseEntity<SandboxAlertConfiguration> cloneSandboxAlertConfiguration(String sandboxId,
      String alertConfigId, CloneSandboxAlertConfigRequest cloneSandboxAlertConfigRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    Optional<Sandbox> sandbox = sandboxService.getSandboxById(sandboxId);
    if (sandbox.isPresent()) {
      try {
        SandboxAlertConfiguration clone = sandboxAlertConfigurationService
            .cloneSandboxAlertConfiguration(sandboxId, alertConfigId,
                cloneSandboxAlertConfigRequest, principal.get());
        sendGuiNotification(clone.getAlertConfigurationUUID(),
            GuiNotificationTypeEnum.SANDBOXALERTCONFIGURATION,
            clone.getUpdatedWhen());
        sendGuiNotification(sandbox.get().getId(),
            GuiNotificationTypeEnum.SANDBOX,
            clone.getUpdatedWhen());
        return ResponseEntity.ok(clone);
      } catch (SandboxAlertConfigurationNotFoundException e) {
        return ResponseEntity.notFound()
            .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
      }
    } else {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  @Override
  public ResponseEntity<SandboxAlertConfiguration> getSandboxAlertConfiguration(String sandboxId,
      String alertConfigurationId) {

    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), UNABLE_TO_FIND_THE_PRINCIPAL).build();
    }
    Optional<Sandbox> sandbox = sandboxService.getSandboxById(sandboxId);
    if (!sandbox.isPresent()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    Optional<SandboxAlertConfiguration> sandboxAlertConfiguration = sandboxAlertConfigurationService
        .getSandboxAlertConfiguration(sandboxId, alertConfigurationId);

    return sandboxAlertConfiguration.isPresent() ? ResponseEntity
        .ok(sandboxAlertConfiguration.get())
        : ResponseEntity.notFound().build();

  }

  public void setGuiNotificationPublisher(
      GuiNotificationPublisher guiNotificationPublisher) {
    this.guiNotificationPublisher = guiNotificationPublisher;
  }

  private void sendGuiNotification(String entityId, GuiNotificationTypeEnum notificationType,
      Instant when) {
    guiNotificationPublisher.sendGuiNotification(
        new GuiNotificationMessage(notificationType.name(), entityId, when));
  }

}