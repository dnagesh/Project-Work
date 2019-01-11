package com.webtech.service.alertconfiguration;

import com.irisium.service.alertconfiguration.api.LiveApiDelegate;
import com.webtech.service.alertconfiguration.exception.AlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.exception.AuditNotFoundException;
import com.webtech.service.alertconfiguration.exception.SandboxAlertConfigurationNotFoundException;
import com.irisium.service.alertconfiguration.model.AlertConfigurationTagsMap;
import com.irisium.service.alertconfiguration.model.EntityLinkRequest;
import com.irisium.service.alertconfiguration.model.EntityRelationship;
import com.irisium.service.alertconfiguration.model.EntityRelationshipAudit;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration;
import com.irisium.service.alertconfiguration.model.LiveAlertConfigurationAudit;
import com.irisium.service.alertconfiguration.model.UpdateStatus;
import com.webtech.service.alertconfiguration.service.AlertConfigurationRelationshipService;
import com.webtech.service.alertconfiguration.service.LiveAlertConfigurationQueryService;
import com.webtech.service.alertconfiguration.service.LiveAlertConfigurationSaveService;
import com.webtech.service.common.security.PrincipalProvider;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.guinotification.GuiNotificationMessage;
import com.webtech.service.guinotification.GuiNotificationPublisher;
import com.webtech.service.guinotification.GuiNotificationTypeEnum;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.webtech.service.common.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class AlertConfigurationApiDelegateImpl implements LiveApiDelegate {

  private final PrincipalProvider principalProvider;
  private final LiveAlertConfigurationSaveService liveAlertConfigurationSaveService;
  private final LiveAlertConfigurationQueryService liveAlertConfigurationQueryService;
  private final AlertConfigurationRelationshipService alertConfigurationRelationshipService;
  private final GuiNotificationPublisher guiNotificationPublisher;

  public AlertConfigurationApiDelegateImpl(
      PrincipalProvider principalProvider,
      LiveAlertConfigurationSaveService liveAlertConfigurationSaveService,
      LiveAlertConfigurationQueryService liveAlertConfigurationQueryService,
      AlertConfigurationRelationshipService alertConfigurationRelationshipService,
      GuiNotificationPublisher guiNotificationPublisher) {
    this.principalProvider = principalProvider;
    this.liveAlertConfigurationSaveService = liveAlertConfigurationSaveService;
    this.liveAlertConfigurationQueryService = liveAlertConfigurationQueryService;
    this.alertConfigurationRelationshipService = alertConfigurationRelationshipService;
    this.guiNotificationPublisher = guiNotificationPublisher;
  }

  @Override
  public ResponseEntity<List<LiveAlertConfiguration>> getAllLiveAlertConfigurations() {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    List<LiveAlertConfiguration> liveAlertConfigurations = liveAlertConfigurationQueryService
        .getAllLiveAlertConfigurations();
    if (CollectionUtils.isEmpty(liveAlertConfigurations)) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(liveAlertConfigurations);
  }

  @Override
  public ResponseEntity<List<LiveAlertConfigurationAudit>> getAllLiveAlertConfigurationsAudit(
      Integer numberOfRecords, Integer maxAge) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    List<LiveAlertConfigurationAudit> alertConfigurationAudits = liveAlertConfigurationQueryService
        .getAllLiveAlertConfigurationsAudit(numberOfRecords, maxAge);
    if (CollectionUtils.isEmpty(alertConfigurationAudits)) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(alertConfigurationAudits);
  }

  @Override
  public ResponseEntity<LiveAlertConfiguration> getLiveAlertConfiguration(
      String alertConfigurationUUID) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    Optional<LiveAlertConfiguration> liveAlertConfiguration = liveAlertConfigurationQueryService
        .getLiveAlertConfigurationById(alertConfigurationUUID);
    return liveAlertConfiguration.isPresent() ? ResponseEntity.ok(liveAlertConfiguration.get())
        : ResponseEntity.notFound().build();

  }

  @Override
  public ResponseEntity<LiveAlertConfigurationAudit> getAuditDetailsForLiveAlertConfiguration(
      String alertConfigurationUUID, String auditUUID) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      LiveAlertConfigurationAudit audit = liveAlertConfigurationQueryService
          .getLiveAlertConfigurationAuditById(alertConfigurationUUID, auditUUID);
      return ResponseEntity.ok(audit);
    } catch (AuditNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (AlertConfigurationNotFoundException e) {
      return ResponseEntity.badRequest().build();
    }


  }

  @Override
  public ResponseEntity<List<LiveAlertConfigurationAudit>> getAuditHistoryForLiveAlertConfiguration(
      String alertConfigurationUUID) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      List<LiveAlertConfigurationAudit> alertConfigurationAudits = liveAlertConfigurationQueryService
          .getAuditHistoryForLiveAlertConfiguration(alertConfigurationUUID);
      if (CollectionUtils.isEmpty(alertConfigurationAudits)) {
        return ResponseEntity.noContent().build();
      }
      return ResponseEntity.ok(alertConfigurationAudits);
    } catch (AlertConfigurationNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @Override
  public ResponseEntity<LiveAlertConfiguration> createLiveAlertConfiguration(String sandboxUUID,
      String sandboxConfigUUID) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      Optional<LiveAlertConfiguration> liveAlertConfiguration = liveAlertConfigurationSaveService
          .createFromSandboxConfiguration(sandboxUUID, sandboxConfigUUID, principal.get());
      if (liveAlertConfiguration.isPresent()) {
        sendGuiNotification(liveAlertConfiguration.get().getAlertConfigurationUUID(),
            GuiNotificationTypeEnum.LIVEALERTCONFIGURATION,
            liveAlertConfiguration.get().getWhen());
        return ResponseEntity.ok(liveAlertConfiguration.get());
      } else {
        return ResponseEntity.unprocessableEntity().build();
      }
    } catch (SandboxAlertConfigurationNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

  }

  @Override
  public ResponseEntity<EntityRelationship> linkTagToLiveAlertConfiguration(
      String alertConfigurationId,
      EntityLinkRequest tagLinkRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      EntityRelationship relationship = alertConfigurationRelationshipService
          .createRelationship(alertConfigurationId, tagLinkRequest,
              EntityType.LIVEALERTCONFIGURATION, EntityType.TAG,
              principal.get(), null);
      sendGuiNotification(relationship.getFromId(),
          GuiNotificationTypeEnum.LIVEALERTCONFIGURATION_REL_TAG,
          relationship.getWhen());
      return ResponseEntity.ok(relationship);
    } catch (AlertConfigurationNotFoundException e) {
      return ResponseEntity.notFound()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<Void> delinkTagFromLiveAlertConfiguration(String alertConfigurationId,
      String tagId) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      EntityRelationship relationship = alertConfigurationRelationshipService
          .deleteRelationship(alertConfigurationId, tagId, EntityType.LIVEALERTCONFIGURATION,
              EntityType.TAG, principal.get(), null);
      sendGuiNotification(relationship.getFromId(),
          GuiNotificationTypeEnum.LIVEALERTCONFIGURATION_REL_TAG,
          relationship.getWhen());
      return ResponseEntity.noContent().build();
    } catch (AlertConfigurationNotFoundException e) {
      return ResponseEntity.badRequest()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<List<String>> getTagsByLiveAlertConfigurationId(
      String alertConfigurationId) {
    List<String> tags = alertConfigurationRelationshipService
        .getRelationshipsByAlertConfigurationId(alertConfigurationId, EntityType.TAG);
    return ResponseEntity.ok(tags);
  }

  @Override
  public ResponseEntity<List<EntityRelationshipAudit>> getLiveAlertConfigurationTagRelationshipAudit(
      String alertConfigurationId,
      String tagId) {
    return getRelationshipAudit(alertConfigurationId, tagId);
  }

  private ResponseEntity<List<EntityRelationshipAudit>> getRelationshipAudit(
      String alertConfigurationId,
      String entityId) {
    try {
      List<EntityRelationshipAudit> auditHistory = alertConfigurationRelationshipService
          .getRelationshipAudit(alertConfigurationId, entityId, null);
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
  public ResponseEntity<LiveAlertConfiguration> updateLiveAlertConfigurationStatus(
      String alertConfigurationUUID, UpdateStatus status) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      LiveAlertConfiguration alertConfiguration = liveAlertConfigurationSaveService
          .updateStatus(alertConfigurationUUID, status, principal.get());
      sendGuiNotification(alertConfigurationUUID,
          GuiNotificationTypeEnum.LIVEALERTCONFIGURATION,
          alertConfiguration.getUpdatedWhen());
      return ResponseEntity.ok(alertConfiguration);
    } catch (AlertConfigurationNotFoundException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @Override
  public ResponseEntity<AlertConfigurationTagsMap> getBulkTagRequestForLiveAlertConfigurationIds(
      List<String> alertConfigurationIds) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    AlertConfigurationTagsMap tagsMap = new AlertConfigurationTagsMap();
    alertConfigurationIds.forEach(alertConfigurationId -> {
      List<String> tags = alertConfigurationRelationshipService
          .getRelationshipsByAlertConfigurationId(alertConfigurationId, EntityType.TAG);
      tagsMap.put(alertConfigurationId, tags);
    });
    return ResponseEntity.ok(tagsMap);
  }

  private void sendGuiNotification(String entityId, GuiNotificationTypeEnum notificationType,
      Instant when) {
    guiNotificationPublisher.sendGuiNotification(
        new GuiNotificationMessage(notificationType.name(), entityId, when));
  }


}
