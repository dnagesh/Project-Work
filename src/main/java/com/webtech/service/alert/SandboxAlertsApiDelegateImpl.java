package com.webtech.service.alert;

import com.webtech.service.alert.service.LiveAlertService;
import com.webtech.service.alert.service.SandboxAlertCommentService;
import com.webtech.service.alert.service.SandboxAlertRelationshipService;
import com.webtech.service.alert.service.SandboxAlertService;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.webtech.service.common.security.PrincipalProvider;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.guinotification.GuiNotificationMessage;
import com.webtech.service.guinotification.GuiNotificationPublisher;
import com.webtech.service.guinotification.GuiNotificationTypeEnum;
import com.irisium.service.livealert.model.Alert;
import com.irisium.service.livealert.model.CreateAlertRequest;
import com.irisium.service.sandboxalert.api.SandboxAlertsApiDelegate;
import com.irisium.service.sandboxalert.model.CreateSandboxAlertRequest;
import com.irisium.service.sandboxalert.model.PromoteSandboxAlertRequest;
import com.irisium.service.sandboxalert.model.SandboxAlert;
import com.irisium.service.sandboxalert.model.SandboxAlertAudit;
import com.irisium.service.sandboxalert.model.SandboxAlertComment;
import com.irisium.service.sandboxalert.model.SandboxAlertCommentCreateRequest;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityLinkRequest;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityRelationship;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityRelationshipAudit;
import com.irisium.service.sandboxalert.model.UpdateSandboxAlertAssigneeRequest;
import com.irisium.service.sandboxalert.model.UpdateSandboxAlertStateRequest;
import java.util.List;
import java.util.Optional;

import com.webtech.service.common.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
public class SandboxAlertsApiDelegateImpl implements SandboxAlertsApiDelegate {

  public static final String UNABLE_TO_FIND_THE_PRINCIPAL = "Unable to find the Principal";
  private final PrincipalProvider principalProvider;
  private final GuiNotificationPublisher guiNotificationPublisher;
  private final SandboxAlertService sandboxAlertService;
  private final SandboxAlertCommentService commentService;
  private final SandboxAlertRelationshipService alertRelationshipService;
  private final LiveAlertService liveAlertService;

  public SandboxAlertsApiDelegateImpl(
      PrincipalProvider principalProvider,
      GuiNotificationPublisher guiNotificationPublisher, SandboxAlertService sandboxAlertService,
      SandboxAlertCommentService commentService,
      SandboxAlertRelationshipService alertRelationshipService,
      LiveAlertService liveAlertService) {
    this.principalProvider = principalProvider;
    this.guiNotificationPublisher = guiNotificationPublisher;
    this.sandboxAlertService = sandboxAlertService;
    this.commentService = commentService;
    this.alertRelationshipService = alertRelationshipService;
    this.liveAlertService = liveAlertService;
  }

  @Override
  public ResponseEntity<SandboxAlert> createSandboxAlert(
      CreateSandboxAlertRequest alertCreateRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (principal.isPresent()) {
      SandboxAlert sandboxAlert = sandboxAlertService.createSandboxAlert(alertCreateRequest);
      guiNotificationPublisher.sendGuiNotification(new GuiNotificationMessage(
          GuiNotificationTypeEnum.SANDBOXALERT.name(), sandboxAlert.getAlertId(),
          sandboxAlert.getCreatedDate()));
      return ResponseEntity.ok(sandboxAlert);
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), UNABLE_TO_FIND_THE_PRINCIPAL).build();
  }

  @Override
  public ResponseEntity<SandboxAlert> getSandboxAlertByIdAndRunId(String sandboxAlertId,
      String runId) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (principal.isPresent()) {
      try {
        SandboxAlert sandboxAlert = sandboxAlertService
            .getSandboxAlertByIdAndRunId(sandboxAlertId, runId);
        return ResponseEntity.ok(sandboxAlert);
      } catch (EntityNotFoundException e) {
        return ResponseEntity.notFound()
            .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
      }
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), UNABLE_TO_FIND_THE_PRINCIPAL).build();
  }

  @Override
  public ResponseEntity<List<SandboxAlertAudit>> getAuditForSandboxAlert(String sandboxAlertId,
      String runId) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (principal.isPresent()) {
      try {
        List<SandboxAlertAudit> audits = sandboxAlertService
            .getAuditForSandboxAlert(sandboxAlertId, runId);
        if (CollectionUtils.isEmpty(audits)) {
          return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(audits);
      } catch (EntityNotFoundException e) {
        return ResponseEntity.notFound()
            .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
      }
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), UNABLE_TO_FIND_THE_PRINCIPAL).build();
  }

  @Override
  public ResponseEntity<List<SandboxAlert>> getAllSandboxAlerts(String sandboxId,
      String runId) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (principal.isPresent()) {
      List<SandboxAlert> alerts = sandboxAlertService.getAllSandboxAlerts(sandboxId, runId);
      if (CollectionUtils.isEmpty(alerts)) {
        return ResponseEntity.noContent().build();
      }
      return ResponseEntity.ok(alerts);
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), UNABLE_TO_FIND_THE_PRINCIPAL).build();
  }

  @Override
  public ResponseEntity<SandboxAlertComment> addSandboxAlertComment(String sandboxAlertId,
      String runId,
      SandboxAlertCommentCreateRequest commentCreateRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      SandboxAlertComment savedComment = commentService
          .addAlertComment(sandboxAlertId, runId, principal.get(), commentCreateRequest);
      return ResponseEntity.ok(savedComment);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<List<SandboxAlertComment>> getAllSandboxAlertComments(String sandboxAlertId,
      String runId) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      List<SandboxAlertComment> comments = commentService
          .getCommentsForAlert(sandboxAlertId, runId);
      if (CollectionUtils.isEmpty(comments)) {
        return ResponseEntity.noContent().build();
      }
      return ResponseEntity.ok(comments);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<SandboxAlert> updateSandboxAlertState(String sandboxAlertId,
      String runId,
      UpdateSandboxAlertStateRequest updateStateRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      SandboxAlert savedAlert = sandboxAlertService.updateAlert(sandboxAlertId, runId,
          updateStateRequest, principal.get());

      if (!StringUtils.isEmpty(updateStateRequest.getReason())) {
        SandboxAlertCommentCreateRequest createRequest = new SandboxAlertCommentCreateRequest();
        createRequest.setComment(updateStateRequest.getReason());
        commentService.addAlertComment(sandboxAlertId, runId, principal.get(), createRequest);
      }
      guiNotificationPublisher.sendGuiNotification(new GuiNotificationMessage(
          GuiNotificationTypeEnum.SANDBOXALERT.name(), savedAlert.getAlertId(),
          savedAlert.getUpdatedDate()));
      return ResponseEntity.ok(savedAlert);

    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<SandboxAlert> updateSandboxAlertAssignee(String sandboxAlertId,
      String runId,
      UpdateSandboxAlertAssigneeRequest updateAssigneeRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      SandboxAlert savedAlert = sandboxAlertService.updateAssignee(sandboxAlertId, runId,
          updateAssigneeRequest, principal.get());
      guiNotificationPublisher.sendGuiNotification(new GuiNotificationMessage(
          GuiNotificationTypeEnum.SANDBOXALERT.name(), savedAlert.getAlertId(),
          savedAlert.getUpdatedDate()));
      return ResponseEntity.ok(savedAlert);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<SandboxAlertEntityRelationship> linkTagToSandboxAlert(String sandboxAlertId,
      String runId,
      SandboxAlertEntityLinkRequest tagLinkRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      SandboxAlertEntityRelationship relationship = alertRelationshipService
          .createRelationship(sandboxAlertId, runId, tagLinkRequest, EntityType.SANDBOXALERT,
              EntityType.TAG, principal.get());
      guiNotificationPublisher.sendGuiNotification(new GuiNotificationMessage(
          GuiNotificationTypeEnum.SANDBOXALERT_REL_TAG.name(), relationship.getFromId(),
          relationship.getWhen()));
      return ResponseEntity.ok(relationship);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<Void> delinkTagFromSandboxAlert(String sandboxAlertId, String runId,
      String tagId) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      SandboxAlertEntityRelationship relationship = alertRelationshipService
          .deleteRelationship(sandboxAlertId, runId, tagId, EntityType.SANDBOXALERT, EntityType.TAG,
              principal.get());
      guiNotificationPublisher.sendGuiNotification(new GuiNotificationMessage(
          GuiNotificationTypeEnum.SANDBOXALERT_REL_TAG.name(), relationship.getFromId(),
          relationship.getWhen()));
      return ResponseEntity.noContent().build();
    } catch (EntityNotFoundException e) {
      return ResponseEntity.badRequest()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<List<SandboxAlertEntityRelationshipAudit>> getSandboxAlertTagRelationshipAudit(
      String sandboxAlertId, String runId, String tagId) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      List<SandboxAlertEntityRelationshipAudit> auditHistory = alertRelationshipService
          .getRelationshipAudit(sandboxAlertId, runId, tagId);

      if (CollectionUtils.isEmpty(auditHistory)) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(auditHistory);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.badRequest()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<List<String>> getTagsBySandboxAlertId(String sandboxAlertId, String runId) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      List<String> tags = alertRelationshipService.getRelationshipsByAlertId(sandboxAlertId, runId,
          EntityType.TAG);
      return ResponseEntity.ok(tags);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<SandboxAlert> promoteSandboxAlertToLive(String sandboxAlertId,
      String runId, PromoteSandboxAlertRequest promoteSandboxAlertRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      SandboxAlert sandboxAlert = sandboxAlertService.getSandboxAlertByIdAndRunId(sandboxAlertId,
          runId);
      CreateAlertRequest request = getCreateAlertRequest(sandboxAlert);
      Alert liveAlert = liveAlertService.createAlert(request);
      SandboxAlert promotedSandboxAlert =
          sandboxAlertService.promoteSandboxAlertToLive(sandboxAlert, principal.get());
      commentService.addAlertComment(sandboxAlertId, runId, principal.get(),
          getCommentCreateRequest(promoteSandboxAlertRequest.getComment()));

      guiNotificationPublisher.sendGuiNotification(new GuiNotificationMessage(
          GuiNotificationTypeEnum.ALERT.name(), liveAlert.getAlertId(),
          liveAlert.getCreatedDate()));
      guiNotificationPublisher.sendGuiNotification(new GuiNotificationMessage(
          GuiNotificationTypeEnum.SANDBOXALERT.name(), promotedSandboxAlert.getAlertId(),
          promotedSandboxAlert.getUpdatedDate()));
      return ResponseEntity.ok(promotedSandboxAlert);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  private SandboxAlertCommentCreateRequest getCommentCreateRequest(String comment) {
    SandboxAlertCommentCreateRequest request = new SandboxAlertCommentCreateRequest();
    request.setComment(comment);
    return request;

  }

  private CreateAlertRequest getCreateAlertRequest(SandboxAlert sandboxAlert) {
    CreateAlertRequest createAlertRequest = new CreateAlertRequest();
    createAlertRequest.setDescription(sandboxAlert.getDescription());
    createAlertRequest.setEndTime(sandboxAlert.getEndTime());
    createAlertRequest.setStartTime(sandboxAlert.getStartTime());
    createAlertRequest
        .setState(CreateAlertRequest.StateEnum.fromValue(sandboxAlert.getState().toString()));
    createAlertRequest.setType(sandboxAlert.getType());
    createAlertRequest.setConfiguration(sandboxAlert.getConfiguration());
    createAlertRequest.setClassification(sandboxAlert.getClassification());
    createAlertRequest.setBusinessUnit(sandboxAlert.getBusinessUnit());
    createAlertRequest.setTitle(sandboxAlert.getTitle());
    createAlertRequest.setInstrumentDescription(sandboxAlert.getInstrumentDescription());
    createAlertRequest.setParticipants(sandboxAlert.getParticipants());
    createAlertRequest.setApsHash(sandboxAlert.getApsHash());

    createAlertRequest.setSandboxAlertId(sandboxAlert.getAlertId());
    createAlertRequest.setRunId(sandboxAlert.getRunId());
    return createAlertRequest;
  }
}
