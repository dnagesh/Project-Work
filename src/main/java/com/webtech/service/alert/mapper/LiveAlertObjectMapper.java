package com.webtech.service.alert.mapper;

import com.datastax.driver.core.utils.UUIDs;
import com.webtech.service.alert.dto.LiveAlertAuditDTO;
import com.webtech.service.alert.dto.LiveAlertAuditDTOPrimaryKey;
import com.webtech.service.alert.dto.LiveAlertCommentDTO;
import com.webtech.service.alert.dto.LiveAlertDTO;
import com.webtech.service.entityrelationship.model.Relationship;
import com.webtech.service.entityrelationship.model.RelationshipAudit;
import com.irisium.service.livealert.model.Alert;
import com.irisium.service.livealert.model.AlertAudit;
import com.irisium.service.livealert.model.Comment;
import com.irisium.service.livealert.model.CommentCreateRequest;
import com.irisium.service.livealert.model.CreateAlertRequest;
import com.irisium.service.livealert.model.EntityRelationship;
import com.irisium.service.livealert.model.EntityRelationshipAudit;
import com.irisium.service.livealert.model.EntityRelationshipAudit.OperationEnum;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
public class LiveAlertObjectMapper {

  public Alert dtoToApi(LiveAlertDTO alertDTO) {
    Alert alert = null;
    if (alertDTO != null) {
      alert = new Alert();
      alert.setAlertId(alertDTO.getAlertId().toString());
      alert.setDescription(alertDTO.getDescription());
      alert.setEndTime(alertDTO.getEndTime());
      alert.setInstrumentDescription(alertDTO.getInstrumentDescription());
      alert.setParticipants(
          alertDTO.getParticipants() != null ? new ArrayList<>(alertDTO.getParticipants()) : null);
      alert.setAssignee(alertDTO.getAssignee());
      alert.setTitle(alertDTO.getTitle());
      alert.setBusinessUnit(alertDTO.getBusinessUnit());
      alert.setClassification(
          alertDTO.getClassification() != null ? new ArrayList<>(alertDTO.getClassification())
              : null);
      alert.setStartTime(alertDTO.getStartTime());
      alert.setState(Alert.StateEnum.fromValue(alertDTO.getState()));
      alert.setConfiguration(alertDTO.getConfiguration());
      alert.setType(alertDTO.getType());

      alert.setApsHash(alertDTO.getApsHash());
      alert.setRunId(Objects.toString(alertDTO.getRunId(), ""));
      alert.setSandboxAlertId(Objects.toString(alertDTO.getSandboxAlertId(), ""));
      alert.setCreatedDate(alertDTO.getCreatedDate());
      alert.setUpdatedBy(alertDTO.getUpdatedBy());
      alert.setUpdatedDate(alertDTO.getUpdatedDate());

    }

    return alert;

  }

  public LiveAlertDTO requestToDto(CreateAlertRequest alertRequest) {
    LiveAlertDTO dto = null;
    if (alertRequest != null) {
      dto = new LiveAlertDTO();
      dto.setDescription(alertRequest.getDescription());
      dto.setEndTime(alertRequest.getEndTime());
      dto.setInstrumentDescription(alertRequest.getInstrumentDescription());
      dto.setParticipants(new HashSet(alertRequest.getParticipants()));
      dto.setTitle(alertRequest.getTitle());
      dto.setBusinessUnit(alertRequest.getBusinessUnit());
      dto.setClassification(new HashSet(alertRequest.getClassification()));
      dto.setStartTime(alertRequest.getStartTime());
      dto.setState(alertRequest.getState().toString());
      dto.setConfiguration(alertRequest.getConfiguration());
      dto.setType(alertRequest.getType());

      dto.setApsHash(alertRequest.getApsHash());
      dto.setCreatedDate(Instant.now());
      dto.setRunId(StringUtils.isEmpty(alertRequest.getRunId()) ? null
          : UUID.fromString(alertRequest.getRunId()));
      dto.setSandboxAlertId(StringUtils.isEmpty(alertRequest.getSandboxAlertId()) ? null
          : UUID.fromString(alertRequest.getSandboxAlertId()));
    }
    return dto;
  }

  public List<Alert> mapList(List<LiveAlertDTO> dtoList) {
    List<Alert> alertList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(dtoList)) {

      alertList = dtoList.stream().map(this::dtoToApi).collect(Collectors.toList());
    }
    return alertList;
  }

  public LiveAlertCommentDTO commentRequestToDto(CommentCreateRequest commentCreateRequest,
                                                 String username) {
    LiveAlertCommentDTO dto = null;
    if (commentCreateRequest != null) {
      dto = new LiveAlertCommentDTO();
      dto.setUsername(username);
      dto.setComment(commentCreateRequest.getComment());
    }
    return dto;
  }

  public Comment commentDtoToApi(LiveAlertCommentDTO commentDTO) {
    Comment comment = null;
    if (commentDTO != null) {
      comment = new Comment();
      comment.setAlertId(commentDTO.getAlertId().toString());
      comment.setCommentId(commentDTO.getCommentId().toString());
      comment.setUsername(commentDTO.getUsername());
      comment.setComment(commentDTO.getComment());
      comment.setCreationTime(commentDTO.getCreationTime());
    }
    return comment;
  }

  public List<Comment> mapCommentsList(List<LiveAlertCommentDTO> commentDtoList) {
    List<Comment> commentList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(commentDtoList)) {

      commentList = commentDtoList.stream().map(this::commentDtoToApi).collect(Collectors.toList());
    }
    return commentList;
  }

  public EntityRelationship relationshipToApi(Relationship relationship) {
    EntityRelationship entityRelationship = null;
    if (relationship != null) {
      entityRelationship = new EntityRelationship();
      entityRelationship.setFromId(relationship.getFromId().toString());
      entityRelationship.setFromType(relationship.getFromType().toString());
      entityRelationship.setToId(relationship.getToId().toString());
      entityRelationship.setToType(relationship.getToType().toString());
      entityRelationship.setWhen(relationship.getWhen());
      entityRelationship.setUser(relationship.getUser());
    }
    return entityRelationship;
  }


  public List<EntityRelationshipAudit> mapRelationshipAuditListToApi(
      List<RelationshipAudit> auditList) {
    List<EntityRelationshipAudit> apiAuditList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(auditList)) {
      apiAuditList = auditList.stream().map(this::relationshipModelToApi)
          .collect(Collectors.toList());
    }
    return apiAuditList;
  }

  private EntityRelationshipAudit relationshipModelToApi(RelationshipAudit relationshipAudit) {
    EntityRelationshipAudit entityRelationshipAudit = null;
    if (relationshipAudit != null) {
      entityRelationshipAudit = new EntityRelationshipAudit();
      entityRelationshipAudit.setFromId(relationshipAudit.getFromId().toString());
      entityRelationshipAudit.setToId(relationshipAudit.getToId().toString());
      entityRelationshipAudit.setFromType(relationshipAudit.getFromType().toString());
      entityRelationshipAudit.setToType(relationshipAudit.getToType().toString());
      entityRelationshipAudit.setUser(relationshipAudit.getOperationWho());
      entityRelationshipAudit.setWhen(relationshipAudit.getWhen());
      entityRelationshipAudit
          .setOperation(OperationEnum.fromValue(relationshipAudit.getRelationshipOperation()));

    }
    return entityRelationshipAudit;
  }

  public LiveAlertAuditDTO alertDTOToAlertAuditDTO(LiveAlertDTO alertDTO) {
    LiveAlertAuditDTO auditDTO = null;
    if (alertDTO != null) {
      auditDTO = new LiveAlertAuditDTO();

      LiveAlertAuditDTOPrimaryKey auditDTOPrimaryKey = new LiveAlertAuditDTOPrimaryKey();
      auditDTOPrimaryKey.setAlertId(alertDTO.getAlertId());
      auditDTOPrimaryKey.setAuditId(UUIDs.timeBased());
      auditDTO.setPrimaryKey(auditDTOPrimaryKey);

      auditDTO.setDescription(alertDTO.getDescription());
      auditDTO.setEndTime(alertDTO.getEndTime());
      auditDTO.setInstrumentDescription(alertDTO.getInstrumentDescription());
      auditDTO.setParticipants(new HashSet(alertDTO.getParticipants()));
      auditDTO.setTitle(alertDTO.getTitle());
      auditDTO.setBusinessUnit(alertDTO.getBusinessUnit());
      auditDTO.setClassification(new HashSet(alertDTO.getClassification()));
      auditDTO.setStartTime(alertDTO.getStartTime());
      auditDTO.setState(alertDTO.getState());
      auditDTO.setAssignee(alertDTO.getAssignee());
      auditDTO.setConfiguration(alertDTO.getConfiguration());
      auditDTO.setType(alertDTO.getType());

      auditDTO.setApsHash(alertDTO.getApsHash());
      auditDTO.setCreatedDate(alertDTO.getCreatedDate());
      auditDTO.setUpdatedBy(alertDTO.getUpdatedBy());
      auditDTO.setUpdatedDate(alertDTO.getUpdatedDate());
      auditDTO.setRunId(alertDTO.getRunId());
      auditDTO.setSandboxAlertId(alertDTO.getSandboxAlertId());

    }
    return auditDTO;
  }

  public List<AlertAudit> mapAuditDTOListToApiList(List<LiveAlertAuditDTO> alertAuditDTOS) {
    List<AlertAudit> alertAudits = Collections.emptyList();
    if (!CollectionUtils.isEmpty(alertAuditDTOS)) {
      alertAudits = alertAuditDTOS.stream().map(this::auditDTOToApi)
          .collect(Collectors.toList());
    }
    return alertAudits;
  }

  private AlertAudit auditDTOToApi(LiveAlertAuditDTO alertAuditDTO) {
    AlertAudit alertAudit = null;
    if (alertAuditDTO != null) {
      alertAudit = new AlertAudit();
      alertAudit.setDescription(alertAuditDTO.getDescription());
      alertAudit.setEndTime(alertAuditDTO.getEndTime());
      alertAudit.setInstrumentDescription(alertAuditDTO.getInstrumentDescription());
      alertAudit.setParticipants(
          alertAuditDTO.getParticipants() != null ? new ArrayList<>(alertAuditDTO.getParticipants())
              : null);
      alertAudit.setAssignee(alertAuditDTO.getAssignee());
      alertAudit.setTitle(alertAuditDTO.getTitle());
      alertAudit.setBusinessUnit(alertAuditDTO.getBusinessUnit());
      alertAudit.setClassification(
          alertAuditDTO.getClassification() != null ? new ArrayList<>(
              alertAuditDTO.getClassification()) : null);
      alertAudit.setStartTime(alertAuditDTO.getStartTime());
      alertAudit.setState(AlertAudit.StateEnum.fromValue(alertAuditDTO.getState()));
      alertAudit.setConfiguration(alertAuditDTO.getConfiguration());
      alertAudit.setType(alertAuditDTO.getType());

      alertAudit.setAlertId(alertAuditDTO.getPrimaryKey().getAlertId().toString());
      alertAudit.setAuditId(alertAuditDTO.getPrimaryKey().getAuditId().toString());
      alertAudit.setApsHash(alertAuditDTO.getApsHash());
      alertAudit.setRunId(Objects.toString(alertAuditDTO.getRunId(), ""));
      alertAudit.setSandboxAlertId(Objects.toString(alertAuditDTO.getSandboxAlertId(), ""));
      alertAudit.setCreatedDate(alertAuditDTO.getCreatedDate());
      alertAudit.setUpdatedBy(alertAuditDTO.getUpdatedBy());
      alertAudit.setUpdatedDate(alertAuditDTO.getUpdatedDate());
    }
    return alertAudit;
  }
}

