package com.webtech.service.alert;

import com.datastax.driver.core.utils.UUIDs;
import com.irisium.TestUtils;
import com.webtech.service.alert.dto.SandboxAlertAuditDTO;
import com.webtech.service.alert.dto.SandboxAlertAuditDTOPrimaryKey;
import com.webtech.service.alert.dto.SandboxAlertCommentDTO;
import com.webtech.service.alert.dto.SandboxAlertDTO;
import com.webtech.service.alert.dto.SandboxAlertDTOPrimaryKey;
import com.irisium.service.alertconfiguration.model.EntityRelationshipAudit.OperationEnum;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.entityrelationship.model.Relationship;
import com.webtech.service.entityrelationship.model.RelationshipAudit;
import com.irisium.service.sandboxalert.model.CreateSandboxAlertRequest;
import com.irisium.service.sandboxalert.model.SandboxAlert;
import com.irisium.service.sandboxalert.model.SandboxAlertAudit;
import com.irisium.service.sandboxalert.model.SandboxAlertComment;
import com.irisium.service.sandboxalert.model.SandboxAlertCommentCreateRequest;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityLinkRequest;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityRelationship;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityRelationshipAudit;
import com.irisium.service.sandboxalert.model.UpdateSandboxAlertAssigneeRequest;
import com.irisium.service.sandboxalert.model.UpdateSandboxAlertStateRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SandboxAlertTestObjects {

  public static CreateSandboxAlertRequest getAlertCreateRequest(String runId, String description,
      String state,
      Instant startTime, Instant endTime, String type, String configuration,
      String instrumentDescription,
      List<String> participants, String title, String businessUnit, List<String> classification) {
    CreateSandboxAlertRequest createAlertRequest = new CreateSandboxAlertRequest();
    createAlertRequest.setRunId(runId);
    createAlertRequest.setDescription(description);
    createAlertRequest.setEndTime(startTime);
    createAlertRequest.setStartTime(endTime);
    createAlertRequest.setState(CreateSandboxAlertRequest.StateEnum.fromValue(state));
    createAlertRequest.setType(type);
    createAlertRequest.setConfiguration(configuration);
    createAlertRequest.setInstrumentDescription(instrumentDescription);
    createAlertRequest.setParticipants(participants);
    createAlertRequest.setTitle(title);
    createAlertRequest.setBusinessUnit(businessUnit);
    createAlertRequest.setClassification(classification);
    return createAlertRequest;
  }

  public static SandboxAlertDTO getAlertDTO(UUID id, UUID runId, String logicDescription,
      String state,
      Instant startTime,
      Instant endTime, String type, String configuration, String instrumentDescription,
      Set<String> participants, String title, String businessUnit, Set<String> classification,
      String assignee) {
    SandboxAlertDTO dto = new SandboxAlertDTO();
    SandboxAlertDTOPrimaryKey primaryKey = new SandboxAlertDTOPrimaryKey(runId, id);
    dto.setPrimaryKey(primaryKey);
    dto.setDescription(logicDescription);
    dto.setState(state);
    dto.setStartTime(startTime);
    dto.setEndTime(endTime);
    dto.setType(type);
    dto.setConfiguration(configuration);
    dto.setInstrumentDescription(instrumentDescription);
    dto.setParticipants(participants);
    dto.setTitle(title);
    dto.setBusinessUnit(businessUnit);
    dto.setClassification(classification);
    dto.setAssignee(assignee);
    dto.setPromotedToLive(true);
    return dto;
  }

  public static List<SandboxAlertDTO> getAlertDTOList() {
    List<SandboxAlertDTO> dtoList = new ArrayList<>();

    dtoList.add(getAlertDTO(UUIDs.timeBased(), UUIDs.timeBased(), "alert 1", "Open",
        TestUtils.randomInstant(),
        TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
        "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
        "Wash Trade", "Europe/Equity", new HashSet<>(Arrays.asList("Regulatory", "Operational")),
        "Dave Jones"));

    dtoList.add(getAlertDTO(UUIDs.timeBased(), UUIDs.timeBased(), "alert 2", "Open",
        TestUtils.randomInstant(),
        TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
        "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
        "Wash Trade", "Europe/Equity", new HashSet<>(Arrays.asList("Regulatory", "Operational")),
        "Dave Jones"));

    return dtoList;
  }

  public static SandboxAlert getAlert(String id, String runId, String description, String state,
      Instant startTime,
      Instant endTime, String type, String configuration, String instrumentDescription,
      List<String> participants, String title, String businessUnit, List<String> classification) {
    SandboxAlert alert = new SandboxAlert();
    alert.setAlertId(id);
    alert.setRunId(runId);
    alert.setDescription(description);
    alert.setState(SandboxAlert.StateEnum.fromValue(state));
    alert.setStartTime(startTime);
    alert.setEndTime(endTime);
    alert.setType(type);
    alert.setConfiguration(configuration);
    alert.setInstrumentDescription(instrumentDescription);
    alert.setParticipants(participants);
    alert.setTitle(title);
    alert.setBusinessUnit(businessUnit);
    alert.setClassification(classification);

    return alert;
  }

  public static List<SandboxAlert> getAlertList() {
    List<SandboxAlert> alertList = new ArrayList<>();
    alertList.add(
        getAlert("1", "1", "alert 1", "Open", TestUtils.randomInstant(), TestUtils.randomInstant(),
            "Abusive Squeeze", "Equity Configuration A", "PO1809 (Palm Olein Future)",
            Arrays.asList("Eleis Commodities"), "Wash Trade", "Europe/Equity",
            Arrays.asList("Regulatory", "Operational")));
    alertList.add(
        getAlert("2", "1", "alert 2", "Open", TestUtils.randomInstant(), TestUtils.randomInstant(),
            "Abusive Squeeze", "Equity Configuration A", "PO1809 (Palm Olein Future)",
            Arrays.asList("Eleis Commodities"), "Wash Trade", "Europe/Equity",
            Arrays.asList("Regulatory", "Operational")));
    return alertList;
  }

  public static SandboxAlertCommentCreateRequest getCommentCreateRequest() {
    SandboxAlertCommentCreateRequest commentCreateRequest = new SandboxAlertCommentCreateRequest();
    commentCreateRequest.setComment("Test comment");
    return commentCreateRequest;
  }

  public static SandboxAlertCommentDTO getCommentDTO(UUID alertId, UUID commentId, String username,
      String comment, Instant creationTime) {
    SandboxAlertCommentDTO dto = new SandboxAlertCommentDTO();
    dto.setAlertId(alertId);
    dto.setCommentId(commentId);
    dto.setUsername(username);
    dto.setComment(comment);
    dto.setCreationTime(creationTime);
    return dto;
  }

  public static List<SandboxAlertCommentDTO> getCommentDTOList() {
    List<SandboxAlertCommentDTO> dtoList = new ArrayList<>();
    dtoList.add(getCommentDTO(TestUtils.randomUUID(), TestUtils.randomUUID(), "User1", "Comment1",
        TestUtils.randomInstant()));
    dtoList.add(getCommentDTO(TestUtils.randomUUID(), TestUtils.randomUUID(), "User2", "Comment2",
        TestUtils.randomInstant()));
    return dtoList;
  }

  public static SandboxAlertComment getComment(UUID alertId, UUID commentId, String username,
      String comment, Instant creationTime) {
    SandboxAlertComment api = new SandboxAlertComment();
    api.setAlertId(alertId.toString());
    api.setCommentId(commentId.toString());
    api.setUsername(username);
    api.setComment(comment);
    api.setCreationTime(creationTime);
    return api;
  }

  public static List<SandboxAlertComment> getCommentList() {
    List<SandboxAlertComment> commentList = new ArrayList<>();
    commentList.add(getComment(TestUtils.randomUUID(), TestUtils.randomUUID(), "User1", "Comment1",
        TestUtils.randomInstant()));
    commentList.add(getComment(TestUtils.randomUUID(), TestUtils.randomUUID(), "User2", "Comment2",
        TestUtils.randomInstant()));
    return commentList;
  }

  public static SandboxAlertEntityLinkRequest getEntityLinkRequest(UUID entityId) {
    SandboxAlertEntityLinkRequest entityLinkRequest = new SandboxAlertEntityLinkRequest();
    entityLinkRequest.setEntityId(entityId.toString());
    return entityLinkRequest;
  }

  public static List<Relationship> getRelationships(UUID fromId) {
    List<Relationship> relationships = new ArrayList<>();
    relationships.add(
        getRelationship(fromId, TestUtils.randomUUID(), EntityType.ALERT, EntityType.TAG,
            TestUtils.randomAlphanumeric(10)));
    relationships.add(
        getRelationship(fromId, TestUtils.randomUUID(), EntityType.ALERT, EntityType.TAG,
            TestUtils.randomAlphanumeric(10)));

    relationships.add(
        getRelationship(fromId, TestUtils.randomUUID(), EntityType.ALERT, EntityType.CASE,
            TestUtils.randomAlphanumeric(10)));
    relationships.add(
        getRelationship(fromId, TestUtils.randomUUID(), EntityType.ALERT, EntityType.CASE,
            TestUtils.randomAlphanumeric(10)));

    return relationships;
  }

  public static Relationship getRelationship(UUID fromId, UUID toId, EntityType fromType,
      EntityType toType, String username) {
    Relationship rel = new Relationship();
    rel.setFromId(fromId);
    rel.setFromType(fromType);
    rel.setToId(toId);
    rel.setToType(toType);
    rel.setUser(username);
    return rel;
  }

  public static SandboxAlertEntityRelationship getEntityRelationship(UUID fromId, UUID toId,
      EntityType fromType,
      EntityType toType, String username) {
    SandboxAlertEntityRelationship rel = new SandboxAlertEntityRelationship();
    rel.setFromId(fromId.toString());
    rel.setFromType(fromType.toString());
    rel.setToId(toId.toString());
    rel.setToType(toType.toString());
    rel.setUser(username);
    return rel;
  }

  public static List<SandboxAlertEntityRelationshipAudit> getRelationshipAuditHistory(UUID fromId,
      UUID toId, EntityType fromType, EntityType toType) {
    List<SandboxAlertEntityRelationshipAudit> auditHistory = new ArrayList<>();
    auditHistory.add(getEntityRelationshipAudit(fromId, toId, fromType.toString(),
        toType.toString(), SandboxAlertEntityRelationshipAudit.OperationEnum.ADD));
    auditHistory.add(getEntityRelationshipAudit(fromId, toId, fromType.toString(),
        toType.toString(), SandboxAlertEntityRelationshipAudit.OperationEnum.REMOVE));
    return auditHistory;
  }

  private static SandboxAlertEntityRelationshipAudit getEntityRelationshipAudit(UUID fromId,
      UUID toId,
      String fromType, String toType, SandboxAlertEntityRelationshipAudit.OperationEnum operation) {
    SandboxAlertEntityRelationshipAudit audit = new SandboxAlertEntityRelationshipAudit();
    audit.setFromId(fromId.toString());
    audit.setToId(toId.toString());
    audit.setOperation(operation);
    audit.setFromType(fromType);
    audit.setToType(toType);
    audit.setUser(TestUtils.randomAlphanumeric(10));
    audit.setWhen(TestUtils.randomInstant());
    return audit;
  }

  public static List<RelationshipAudit> getRelationshipAudits(UUID fromId, UUID toId,
                                                              EntityType fromType, EntityType toType) {
    List<RelationshipAudit> relationshipAudits = new ArrayList<>();
    relationshipAudits.add(getRelationshipAudit(fromId, toId, fromType, toType, OperationEnum.ADD));
    relationshipAudits
        .add(getRelationshipAudit(fromId, toId, fromType, toType, OperationEnum.REMOVE));
    return relationshipAudits;
  }

  private static RelationshipAudit getRelationshipAudit(UUID fromId, UUID toId,
      EntityType fromType, EntityType toType, OperationEnum operation) {
    RelationshipAudit relationshipAudit = new RelationshipAudit();
    relationshipAudit.setFromId(fromId);
    relationshipAudit.setToId(toId);
    relationshipAudit.setFromType(fromType);
    relationshipAudit.setToType(toType);
    relationshipAudit.setOperationWho(TestUtils.randomAlphanumeric(10));
    relationshipAudit.setWhen(TestUtils.randomInstant());
    relationshipAudit.setRelationshipOperation(operation.toString());
    return relationshipAudit;
  }

  public static List<RelationshipAudit> getRelationshipAuditsWithNulls() {
    List<RelationshipAudit> relationshipAudits = new ArrayList<>();
    relationshipAudits.add(null);
    return relationshipAudits;
  }

  public static UpdateSandboxAlertStateRequest getUpdateStateRequest(String state, String reason) {
    UpdateSandboxAlertStateRequest request = new UpdateSandboxAlertStateRequest();
    request.setState(UpdateSandboxAlertStateRequest.StateEnum.fromValue(state));
    request.setReason(reason);
    return request;
  }

  public static UpdateSandboxAlertAssigneeRequest getUpdateAssigneeRequest(String assignee) {
    UpdateSandboxAlertAssigneeRequest request = new UpdateSandboxAlertAssigneeRequest();
    request.setAssignee(assignee);
    return request;
  }

  public static List<SandboxAlertAuditDTO> getAlertAuditDTOs() {
    List<SandboxAlertAuditDTO> alertAuditDTOS = new ArrayList<>();
    alertAuditDTOS
        .add(getAlertAuditDTO(UUIDs.timeBased(), UUIDs.timeBased(), UUIDs.timeBased(), "alert 1",
            "Open",
            TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
            "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
            "Dave Jones", TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(10)));

    return alertAuditDTOS;
  }

  private static SandboxAlertAuditDTO getAlertAuditDTO(UUID id, UUID auditId, UUID runId,
      String description,
      String state,
      Instant startTime, Instant endTime, String type,
      String configuration, String instrumentDescription, Set<String> participants, String title,
      String businessUnit, Set<String> classification, String assignee, Instant when, String who) {
    SandboxAlertAuditDTO alertAuditDTO = new SandboxAlertAuditDTO();
    alertAuditDTO.setState(state);
    alertAuditDTO.setDescription(description);
    alertAuditDTO.setEndTime(endTime);
    alertAuditDTO.setStartTime(startTime);
    alertAuditDTO.setInstrumentDescription(instrumentDescription);
    alertAuditDTO.setParticipants(participants);
    alertAuditDTO.setType(type);
    alertAuditDTO.setConfiguration(configuration);
    alertAuditDTO.setTitle(title);
    alertAuditDTO.setBusinessUnit(businessUnit);
    alertAuditDTO.setClassification(classification);
    alertAuditDTO.setAssignee(assignee);
    SandboxAlertAuditDTOPrimaryKey alertAuditDTOPrimaryKey = new SandboxAlertAuditDTOPrimaryKey(id,
        auditId);
    alertAuditDTO.setUpdatedBy(who);
    alertAuditDTO.setPrimaryKey(alertAuditDTOPrimaryKey);
    alertAuditDTO.setRunId(runId);

    return alertAuditDTO;
  }

  public static List<SandboxAlertAuditDTO> getAlertAuditsWithNulls() {
    List<SandboxAlertAuditDTO> alertAudits = new ArrayList<>();
    alertAudits.add(null);
    return alertAudits;
  }

  public static List<SandboxAlertAudit> getAlertAudits(UUID alertId) {
    List<SandboxAlertAudit> alertAudits = new ArrayList<>();
    alertAudits.add(
        getAlertAudit(alertId, TestUtils.randomUUID(), "alert 1", "Open", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", Arrays.asList("Eleis Commodities"), "Wash Trade",
            "Europe/Equity", Arrays.asList("Regulatory", "Operational"), "Dave Jones",
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(10)));

    return alertAudits;
  }

  private static SandboxAlertAudit getAlertAudit(UUID id, UUID auditId, String description,
      String state,
      Instant startTime,
      Instant endTime, String type,
      String configuration, String instrumentDescription, List<String> participants, String title,
      String businessUnit, List<String> classification, String assignee, Instant when, String who) {
    SandboxAlertAudit alertAudit = new SandboxAlertAudit();
    alertAudit.setState(SandboxAlertAudit.StateEnum.fromValue(state));
    alertAudit.setDescription(description);
    alertAudit.setEndTime(endTime);
    alertAudit.setStartTime(startTime);
    alertAudit.setInstrumentDescription(instrumentDescription);
    alertAudit.setParticipants(participants);
    alertAudit.setType(type);
    alertAudit.setConfiguration(configuration);
    alertAudit.setTitle(title);
    alertAudit.setBusinessUnit(businessUnit);
    alertAudit.setClassification(classification);
    alertAudit.setAssignee(assignee);
    alertAudit.setUpdatedBy(who);
    alertAudit.setUpdatedDate(when);
    alertAudit.setAlertId(id.toString());
    alertAudit.setAuditId(auditId.toString());
    return alertAudit;
  }
}
