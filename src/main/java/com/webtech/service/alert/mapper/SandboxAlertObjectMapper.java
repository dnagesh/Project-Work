package com.webtech.service.alert.mapper;

import com.datastax.driver.core.utils.UUIDs;
import com.webtech.service.alert.dto.SandboxAlertAuditDTO;
import com.webtech.service.alert.dto.SandboxAlertAuditDTOPrimaryKey;
import com.webtech.service.alert.dto.SandboxAlertCommentDTO;
import com.webtech.service.alert.dto.SandboxAlertDTO;
import com.webtech.service.alert.dto.SandboxAlertDTOPrimaryKey;
import com.webtech.service.entityrelationship.model.Relationship;
import com.webtech.service.entityrelationship.model.RelationshipAudit;
import com.irisium.service.sandboxalert.model.CreateSandboxAlertRequest;
import com.irisium.service.sandboxalert.model.SandboxAlert;
import com.irisium.service.sandboxalert.model.SandboxAlertAudit;
import com.irisium.service.sandboxalert.model.SandboxAlertComment;
import com.irisium.service.sandboxalert.model.SandboxAlertCommentCreateRequest;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityRelationship;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityRelationshipAudit;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class SandboxAlertObjectMapper {

  public SandboxAlertDTO requestToDTO(CreateSandboxAlertRequest alertCreateRequest) {
    SandboxAlertDTO dto = null;
    if (alertCreateRequest != null) {
      dto = new SandboxAlertDTO();
      dto.setApsHash(alertCreateRequest.getApsHash());
      dto.setAssignee(alertCreateRequest.getAssignee());
      dto.setBusinessUnit(alertCreateRequest.getBusinessUnit());
      dto.setClassification(new HashSet<>(alertCreateRequest.getClassification()));
      dto.setConfiguration(alertCreateRequest.getConfiguration());
      dto.setCreatedDate(Instant.now());
      dto.setDescription(alertCreateRequest.getDescription());
      dto.setEndTime(alertCreateRequest.getEndTime());
      dto.setInstrumentDescription(alertCreateRequest.getInstrumentDescription());
      dto.setParticipants(new HashSet<>(alertCreateRequest.getParticipants()));
      dto.setStartTime(alertCreateRequest.getStartTime());
      dto.setState(alertCreateRequest.getState().toString());
      dto.setTitle(alertCreateRequest.getTitle());
      dto.setType(alertCreateRequest.getType());
      dto.setPrimaryKey(
          new SandboxAlertDTOPrimaryKey(UUID.fromString(alertCreateRequest.getRunId()),
              UUIDs.timeBased()));
    }
    return dto;
  }

  public SandboxAlert dtoToApi(SandboxAlertDTO dto) {
    SandboxAlert alert = null;
    if (dto != null) {
      alert = new SandboxAlert();
      alert.setAlertId(dto.getPrimaryKey().getAlertId().toString());
      alert.setRunId(dto.getPrimaryKey().getRunId().toString());
      alert.setAssignee(dto.getAssignee());
      alert.setApsHash(dto.getApsHash());
      alert.setBusinessUnit(dto.getBusinessUnit());
      alert.setCreatedDate(dto.getCreatedDate());
      alert.setClassification(getDataList(dto.getClassification()));
      alert.setConfiguration(dto.getConfiguration());
      alert.setDescription(dto.getDescription());
      alert.setEndTime(dto.getEndTime());
      alert.setInstrumentDescription(dto.getInstrumentDescription());
      alert.setParticipants(getDataList(dto.getParticipants()));
      alert.setTitle(dto.getTitle());
      alert.setStartTime(dto.getStartTime());
      alert.setState(SandboxAlert.StateEnum.fromValue(dto.getState()));
      alert.setType(dto.getType());
      alert.setPromotedToLive(dto.isPromotedToLive());
      alert.setUpdatedDate(dto.getUpdatedDate());
      alert.setUpdatedBy(dto.getUpdatedBy());
      alert.setPromotedToLive(dto.isPromotedToLive());

    }

    return alert;
  }

  private List<String> getDataList(Set<String> dataSet) {
    if (dataSet != null) {
      return new ArrayList<>(dataSet);
    }
    return Collections.emptyList();
  }

  public SandboxAlertAudit auditDtoToApi(SandboxAlertAuditDTO dto) {
    SandboxAlertAudit alertAudit = null;
    if (dto != null) {
      alertAudit = new SandboxAlertAudit();
      alertAudit.setAlertId(dto.getPrimaryKey().getAlertId().toString());
      alertAudit.setAuditId(dto.getPrimaryKey().getAuditId().toString());
      alertAudit.setRunId(dto.getRunId().toString());
      alertAudit.setAssignee(dto.getAssignee());
      alertAudit.setApsHash(dto.getApsHash());
      alertAudit.setBusinessUnit(dto.getBusinessUnit());
      alertAudit.setCreatedDate(dto.getCreatedDate());
      alertAudit.setClassification(getDataList(dto.getClassification()));
      alertAudit.setConfiguration(dto.getConfiguration());
      alertAudit.setDescription(dto.getDescription());
      alertAudit.setEndTime(dto.getEndTime());
      alertAudit.setInstrumentDescription(dto.getInstrumentDescription());
      alertAudit.setParticipants(getDataList(dto.getParticipants()));
      alertAudit.setTitle(dto.getTitle());
      alertAudit.setStartTime(dto.getStartTime());
      alertAudit.setState(SandboxAlertAudit.StateEnum.fromValue(dto.getState()));
      alertAudit.setType(dto.getType());
      alertAudit.setPromotedToLive(dto.isPromotedToLive());
      alertAudit.setUpdatedDate(dto.getUpdatedDate());
      alertAudit.setUpdatedBy(dto.getUpdatedBy());
    }
    return alertAudit;
  }

  public List<SandboxAlertAudit> auditDTOListToApi(List<SandboxAlertAuditDTO> audits) {
    List<SandboxAlertAudit> apiList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(audits)) {
      apiList = audits.stream().map(this::auditDtoToApi).filter(Objects::nonNull)
          .collect(Collectors.toList());
    }
    return apiList;
  }

  public SandboxAlertAuditDTO dtoToAuditDTO(SandboxAlertDTO dto) {
    SandboxAlertAuditDTO auditDTO = null;
    if (dto != null) {
      auditDTO = new SandboxAlertAuditDTO();
      SandboxAlertAuditDTOPrimaryKey primaryKey = new SandboxAlertAuditDTOPrimaryKey(
          dto.getPrimaryKey().getAlertId(), UUIDs.timeBased());
      auditDTO.setPrimaryKey(primaryKey);
      auditDTO.setRunId(dto.getPrimaryKey().getRunId());
      auditDTO.setAssignee(dto.getAssignee());
      auditDTO.setApsHash(dto.getApsHash());
      auditDTO.setBusinessUnit(dto.getBusinessUnit());
      auditDTO.setCreatedDate(dto.getCreatedDate());
      auditDTO.setClassification(dto.getClassification());
      auditDTO.setConfiguration(dto.getConfiguration());
      auditDTO.setDescription(dto.getDescription());
      auditDTO.setEndTime(dto.getEndTime());
      auditDTO.setInstrumentDescription(dto.getInstrumentDescription());
      auditDTO.setParticipants(dto.getParticipants());
      auditDTO.setTitle(dto.getTitle());
      auditDTO.setStartTime(dto.getStartTime());
      auditDTO.setState(dto.getState());
      auditDTO.setType(dto.getType());
      auditDTO.setPromotedToLive(dto.isPromotedToLive());
      auditDTO.setUpdatedDate(dto.getUpdatedDate());
      auditDTO.setUpdatedBy(dto.getUpdatedBy());
    }
    return auditDTO;
  }

  public List<SandboxAlert> dtoListToApi(List<SandboxAlertDTO> dtos) {
    List<SandboxAlert> apiList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(dtos)) {
      apiList = dtos.stream().map(this::dtoToApi).filter(Objects::nonNull)
          .collect(Collectors.toList());
    }
    return apiList;
  }

  public SandboxAlertCommentDTO commentRequestToDto(
      SandboxAlertCommentCreateRequest commentCreateRequest, String username) {
    SandboxAlertCommentDTO dto = null;
    if (commentCreateRequest != null) {
      dto = new SandboxAlertCommentDTO();
      dto.setCommentId(UUIDs.timeBased());
      dto.setUsername(username);
      dto.setComment(commentCreateRequest.getComment());
      dto.setCreationTime(Instant.now());
    }
    return dto;
  }

  public SandboxAlertComment commentDtoToApi(SandboxAlertCommentDTO commentDTO) {
    SandboxAlertComment comment = null;
    if (commentDTO != null) {
      comment = new SandboxAlertComment();
      comment.setAlertId(commentDTO.getAlertId().toString());
      comment.setCommentId(commentDTO.getCommentId().toString());
      comment.setUsername(commentDTO.getUsername());
      comment.setComment(commentDTO.getComment());
      comment.setCreationTime(commentDTO.getCreationTime());
    }
    return comment;
  }

  public List<SandboxAlertComment> mapCommentsList(List<SandboxAlertCommentDTO> commentDtoList) {
    List<SandboxAlertComment> commentList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(commentDtoList)) {

      commentList = commentDtoList.stream().map(this::commentDtoToApi).collect(Collectors.toList());
    }
    return commentList;
  }

  public SandboxAlertEntityRelationship relationshipToApi(Relationship relationship) {
    SandboxAlertEntityRelationship entityRelationship = null;
    if (relationship != null) {
      entityRelationship = new SandboxAlertEntityRelationship();
      entityRelationship.setFromId(relationship.getFromId().toString());
      entityRelationship.setFromType(relationship.getFromType().toString());
      entityRelationship.setToId(relationship.getToId().toString());
      entityRelationship.setToType(relationship.getToType().toString());
      entityRelationship.setWhen(relationship.getWhen());
      entityRelationship.setUser(relationship.getUser());
    }
    return entityRelationship;
  }

  public List<SandboxAlertEntityRelationshipAudit> mapRelationshipAuditListToApi(
      List<RelationshipAudit> auditList) {
    List<SandboxAlertEntityRelationshipAudit> apiAuditList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(auditList)) {
      apiAuditList = auditList.stream().map(this::relationshipModelToApi)
          .collect(Collectors.toList());
    }
    return apiAuditList;
  }

  private SandboxAlertEntityRelationshipAudit relationshipModelToApi(
      RelationshipAudit relationshipAudit) {
    SandboxAlertEntityRelationshipAudit entityRelationshipAudit = null;
    if (relationshipAudit != null) {
      entityRelationshipAudit = new SandboxAlertEntityRelationshipAudit();
      entityRelationshipAudit.setFromId(relationshipAudit.getFromId().toString());
      entityRelationshipAudit.setToId(relationshipAudit.getToId().toString());
      entityRelationshipAudit.setFromType(relationshipAudit.getFromType().toString());
      entityRelationshipAudit.setToType(relationshipAudit.getToType().toString());
      entityRelationshipAudit.setUser(relationshipAudit.getOperationWho());
      entityRelationshipAudit.setWhen(relationshipAudit.getWhen());
      entityRelationshipAudit
          .setOperation(SandboxAlertEntityRelationshipAudit.OperationEnum
              .fromValue(relationshipAudit.getRelationshipOperation()));

    }
    return entityRelationshipAudit;
  }
}
