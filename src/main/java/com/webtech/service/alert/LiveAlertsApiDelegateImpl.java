package com.webtech.service.alert;

import com.webtech.service.alert.exception.UpdateStateReasonMissingException;
import com.webtech.service.alert.service.LiveAlertCommentService;
import com.webtech.service.alert.service.LiveAlertRelationshipService;
import com.webtech.service.alert.service.LiveAlertService;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.webtech.service.common.security.PrincipalProvider;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.guinotification.GuiNotificationMessage;
import com.webtech.service.guinotification.GuiNotificationPublisher;
import com.webtech.service.guinotification.GuiNotificationTypeEnum;
import com.irisium.service.livealert.api.LiveAlertsApiDelegate;
import com.irisium.service.livealert.model.Alert;
import com.irisium.service.livealert.model.AlertAudit;
import com.irisium.service.livealert.model.Comment;
import com.irisium.service.livealert.model.CommentCreateRequest;
import com.irisium.service.livealert.model.CreateAlertRequest;
import com.irisium.service.livealert.model.EntityLinkRequest;
import com.irisium.service.livealert.model.EntityRelationship;
import com.irisium.service.livealert.model.EntityRelationshipAudit;
import com.irisium.service.livealert.model.UpdateAssigneeRequest;
import com.irisium.service.livealert.model.UpdateStateRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.webtech.service.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
public class LiveAlertsApiDelegateImpl implements LiveAlertsApiDelegate {

  private final GuiNotificationPublisher guiNotificationPublisher;
  private final PrincipalProvider principalProvider;
  private LiveAlertService service;
  private LiveAlertCommentService commentService;
  private LiveAlertRelationshipService alertRelationshipService;

  @Autowired
  public LiveAlertsApiDelegateImpl(LiveAlertService service, LiveAlertCommentService commentService,
      LiveAlertRelationshipService alertRelationshipService,
      GuiNotificationPublisher guiNotificationPublisher, PrincipalProvider principalProvider) {
    this.service = service;
    this.commentService = commentService;
    this.alertRelationshipService = alertRelationshipService;
    this.guiNotificationPublisher = guiNotificationPublisher;
    this.principalProvider = principalProvider;
  }

  @Override
  public ResponseEntity<Alert> createAlert(CreateAlertRequest alertCreateRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    Alert savedAlert = service.createAlert(alertCreateRequest);
    sendGuiNotification(savedAlert.getAlertId(), GuiNotificationTypeEnum.ALERT,
        savedAlert.getCreatedDate());
    return ResponseEntity.ok(savedAlert);
  }


  @Override
  public ResponseEntity<List<Alert>> getAllAlerts() {
    List<Alert> alertList = service.getAllAlerts();
    return ResponseEntity.ok(alertList);
  }

  @Override
  public ResponseEntity<Alert> getAlertById(String alertId) {
    Optional<Alert> alert = service.getAlertById(alertId);
    if (alert.isPresent()) {
      return ResponseEntity.ok(alert.get());
    }
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @Override
  public ResponseEntity<Alert> updateAlertState(String alertId,
      UpdateStateRequest updateStateRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      Alert savedAlert = service.updateAlert(alertId, updateStateRequest, principal.get());

      if (!StringUtils.isEmpty(updateStateRequest.getReason())) {
        CommentCreateRequest commentCreateRequest = new CommentCreateRequest();
        commentCreateRequest.setComment(updateStateRequest.getReason());
        commentService.addAlertComment(alertId, principal.get(), commentCreateRequest);
      }
      sendGuiNotification(savedAlert.getAlertId(), GuiNotificationTypeEnum.ALERT,
          savedAlert.getUpdatedDate());
      return ResponseEntity.ok(savedAlert);
    } catch (EntityNotFoundException | UpdateStateReasonMissingException e) {
      return ResponseEntity.notFound()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }


  @Override
  public ResponseEntity<Alert> updateAssignee(String alertId,
      UpdateAssigneeRequest updateAssigneeRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      Alert savedAlert = service.updateAssignee(alertId, updateAssigneeRequest, principal.get());
      sendGuiNotification(savedAlert.getAlertId(), GuiNotificationTypeEnum.ALERT,
          savedAlert.getUpdatedDate());
      return ResponseEntity.ok(savedAlert);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<Comment> addComment(String alertId,
      CommentCreateRequest commentCreateRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      Comment savedComment = commentService
          .addAlertComment(alertId, principal.get(), commentCreateRequest);
      return ResponseEntity.ok(savedComment);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<List<Comment>> getAllComments(String alertId) {
    List<Comment> comments = commentService.getCommentsForAlert(alertId);
    return ResponseEntity.ok(comments);
  }

  @Override
  public ResponseEntity<EntityRelationship> linkTagToAlert(String alertId,
      EntityLinkRequest tagLinkRequest) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      EntityRelationship relationship = alertRelationshipService
          .createRelationship(alertId, tagLinkRequest, EntityType.ALERT, EntityType.TAG,
              principal.get());
      sendGuiNotification(relationship.getFromId(),
          GuiNotificationTypeEnum.ALERT_REL_TAG,
          relationship.getWhen());
      return ResponseEntity.ok(relationship);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<Void> delinkTagFromAlert(String alertId,
      String tagId) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      EntityRelationship relationship = alertRelationshipService
          .deleteRelationship(alertId, tagId, EntityType.ALERT, EntityType.TAG, principal.get());
      sendGuiNotification(relationship.getFromId(),
          GuiNotificationTypeEnum.ALERT_REL_TAG,
          relationship.getWhen());
      return ResponseEntity.noContent().build();
    } catch (EntityNotFoundException e) {
      return ResponseEntity.badRequest()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<List<String>> getTagsByAlertId(String alertId) {
    List<String> tags = alertRelationshipService.getRelationshipsByAlertId(alertId, EntityType.TAG);
    return ResponseEntity.ok(tags);
  }

  @Override
  public ResponseEntity<List<String>> getCasesByAlertId(String alertId) {
    List<String> tags = alertRelationshipService
        .getRelationshipsByAlertId(alertId, EntityType.CASE);
    return ResponseEntity.ok(tags);
  }

  //Audit API
  @Override
  public ResponseEntity<List<AlertAudit>> getAuditForAlert(String alertId) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      List<AlertAudit> auditHistory = service.getAudit(alertId);

      if (CollectionUtils.isEmpty(auditHistory)) {
        return ResponseEntity.noContent().build();
      }
      return ResponseEntity.ok(auditHistory);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  @Override
  public ResponseEntity<List<EntityRelationshipAudit>> getAlertTagRelationshipAudit(String alertId,
      String tagId) {
    return getRelationshipAudit(alertId, tagId);
  }

  @Override
  public ResponseEntity<List<EntityRelationshipAudit>> getAlertCaseRelationshipAudit(String alertId,
      String caseId) {
    return getRelationshipAudit(alertId, caseId);
  }

  private ResponseEntity<List<EntityRelationshipAudit>> getRelationshipAudit(String alertId,
      String entityId) {
    Optional<String> principal = principalProvider.getPrincipal();
    if (!principal.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      List<EntityRelationshipAudit> auditHistory = alertRelationshipService
          .getRelationshipAudit(alertId, entityId);

      if (CollectionUtils.isEmpty(auditHistory)) {
        return ResponseEntity.notFound().build();
      }
      return ResponseEntity.ok(auditHistory);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.badRequest()
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
  }

  private void sendGuiNotification(String entityId, GuiNotificationTypeEnum notificationType,
      Instant when) {
    guiNotificationPublisher.sendGuiNotification(
        new GuiNotificationMessage(notificationType.name(), entityId, when));
  }
}
