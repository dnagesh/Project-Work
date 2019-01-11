package com.webtech.service.alert;

import com.datastax.driver.core.utils.UUIDs;
import com.irisium.TestUtils;
import com.webtech.service.alert.dto.LiveAlertAuditDTO;
import com.webtech.service.alert.dto.LiveAlertAuditDTOPrimaryKey;
import com.webtech.service.alert.dto.LiveAlertCommentDTO;
import com.webtech.service.alert.dto.LiveAlertDTO;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.entityrelationship.model.Relationship;
import com.webtech.service.entityrelationship.model.RelationshipAudit;
import com.irisium.service.livealert.model.Alert;
import com.irisium.service.livealert.model.AlertAudit;
import com.irisium.service.livealert.model.Comment;
import com.irisium.service.livealert.model.CommentCreateRequest;
import com.irisium.service.livealert.model.CreateAlertRequest;
import com.irisium.service.livealert.model.EntityLinkRequest;
import com.irisium.service.livealert.model.EntityRelationship;
import com.irisium.service.livealert.model.EntityRelationshipAudit;
import com.irisium.service.livealert.model.EntityRelationshipAudit.OperationEnum;
import com.irisium.service.livealert.model.UpdateAssigneeRequest;
import com.irisium.service.livealert.model.UpdateStateRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LiveAlertTestObjects {

  public static CreateAlertRequest getAlertCreateRequest(String description, String state,
      Instant startTime, Instant endTime, String type, String configuration,
      String instrumentDescription,
      List<String> participants, String title, String businessUnit, List<String> classification) {
    CreateAlertRequest createAlertRequest = new CreateAlertRequest();
    createAlertRequest.setDescription(description);
    createAlertRequest.setEndTime(startTime);
    createAlertRequest.setStartTime(endTime);
    createAlertRequest.setState(CreateAlertRequest.StateEnum.fromValue(state));
    createAlertRequest.setType(type);
    createAlertRequest.setConfiguration(configuration);
    createAlertRequest.setInstrumentDescription(instrumentDescription);
    createAlertRequest.setParticipants(participants);
    createAlertRequest.setTitle(title);
    createAlertRequest.setBusinessUnit(businessUnit);
    createAlertRequest.setClassification(classification);
    createAlertRequest.setApsHash(TestUtils.randomAlphanumeric(5));
    return createAlertRequest;
  }

  public static LiveAlertDTO getAlertDTO(UUID id, String logicDescription, String state,
      Instant startTime,
      Instant endTime, String type, String configuration, String instrumentDescription,
      Set<String> participants, String title, String businessUnit, Set<String> classification,
      String assignee) {
    LiveAlertDTO dto = new LiveAlertDTO();
    dto.setAlertId(id);
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
    dto.setApsHash(TestUtils.randomAlphanumeric(5));
    dto.setCreatedDate(TestUtils.randomInstant());
    dto.setUpdatedBy(TestUtils.randomAlphanumeric(5));
    dto.setUpdatedDate(TestUtils.randomInstant());
    dto.setRunId(TestUtils.randomUUID());
    dto.setSandboxAlertId(TestUtils.randomUUID());
    return dto;
  }

  public static List<LiveAlertDTO> getAlertDTOList() {
    List<LiveAlertDTO> dtoList = new ArrayList<>();

    dtoList.add(getAlertDTO(UUIDs.timeBased(), "alert 1", "Open", TestUtils.randomInstant(),
        TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
        "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
        "Wash Trade", "Europe/Equity", new HashSet<>(Arrays.asList("Regulatory", "Operational")),
        "Dave Jones"));

    dtoList.add(getAlertDTO(UUIDs.timeBased(), "alert 2", "Open", TestUtils.randomInstant(),
        TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
        "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
        "Wash Trade", "Europe/Equity", new HashSet<>(Arrays.asList("Regulatory", "Operational")),
        "Dave Jones"));

    return dtoList;
  }

  public static Alert getAlert(String id, String description, String state, Instant startTime,
      Instant endTime, String type, String configuration, String instrumentDescription,
      List<String> participants, String title, String businessUnit, List<String> classification) {
    Alert alert = new Alert();
    alert.setAlertId(id);
    alert.setDescription(description);
    alert.setState(Alert.StateEnum.fromValue(state));
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

  public static List<Alert> getAlertList() {
    List<Alert> alertList = new ArrayList<>();
    alertList.add(
        getAlert("1", "alert 1", "Open", TestUtils.randomInstant(), TestUtils.randomInstant(),
            "Abusive Squeeze", "Equity Configuration A", "PO1809 (Palm Olein Future)",
            Arrays.asList("Eleis Commodities"), "Wash Trade", "Europe/Equity",
            Arrays.asList("Regulatory", "Operational")));
    alertList.add(
        getAlert("2", "alert 2", "Open", TestUtils.randomInstant(), TestUtils.randomInstant(),
            "Abusive Squeeze", "Equity Configuration A", "PO1809 (Palm Olein Future)",
            Arrays.asList("Eleis Commodities"), "Wash Trade", "Europe/Equity",
            Arrays.asList("Regulatory", "Operational")));
    return alertList;
  }

  public static CommentCreateRequest getCommentCreateRequest() {
    CommentCreateRequest commentCreateRequest = new CommentCreateRequest();
    commentCreateRequest.setComment("Test comment");
    return commentCreateRequest;
  }

  public static LiveAlertCommentDTO getCommentDTO(UUID alertId, UUID commentId, String username,
      String comment, Instant creationTime) {
    LiveAlertCommentDTO dto = new LiveAlertCommentDTO();
    dto.setAlertId(alertId);
    dto.setCommentId(commentId);
    dto.setUsername(username);
    dto.setComment(comment);
    dto.setCreationTime(creationTime);
    return dto;
  }

  public static List<LiveAlertCommentDTO> getCommentDTOList() {
    List<LiveAlertCommentDTO> dtoList = new ArrayList<>();
    dtoList.add(getCommentDTO(TestUtils.randomUUID(), TestUtils.randomUUID(), "User1", "Comment1",
        TestUtils.randomInstant()));
    dtoList.add(getCommentDTO(TestUtils.randomUUID(), TestUtils.randomUUID(), "User2", "Comment2",
        TestUtils.randomInstant()));
    return dtoList;
  }

  public static Comment getComment(UUID alertId, UUID commentId, String username,
      String comment, Instant creationTime) {
    Comment api = new Comment();
    api.setAlertId(alertId.toString());
    api.setCommentId(commentId.toString());
    api.setUsername(username);
    api.setComment(comment);
    api.setCreationTime(creationTime);
    return api;
  }

  public static List<Comment> getCommentList() {
    List<Comment> commentList = new ArrayList<>();
    commentList.add(getComment(TestUtils.randomUUID(), TestUtils.randomUUID(), "User1", "Comment1",
        TestUtils.randomInstant()));
    commentList.add(getComment(TestUtils.randomUUID(), TestUtils.randomUUID(), "User2", "Comment2",
        TestUtils.randomInstant()));
    return commentList;
  }

  public static EntityLinkRequest getEntityLinkRequest(UUID entityId) {
    EntityLinkRequest entityLinkRequest = new EntityLinkRequest();
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

  public static EntityRelationship getEntityRelationship(UUID fromId, UUID toId,
      EntityType fromType,
      EntityType toType, String username) {
    EntityRelationship rel = new EntityRelationship();
    rel.setFromId(fromId.toString());
    rel.setFromType(fromType.toString());
    rel.setToId(toId.toString());
    rel.setToType(toType.toString());
    rel.setUser(username);
    return rel;
  }

  public static List<EntityRelationshipAudit> getRelationshipAuditHistory(UUID fromId,
      UUID toId, EntityType fromType, EntityType toType) {
    List<EntityRelationshipAudit> auditHistory = new ArrayList<>();
    auditHistory.add(getEntityRelationshipAudit(fromId, toId, fromType.toString(),
        toType.toString(), OperationEnum.ADD));
    auditHistory.add(getEntityRelationshipAudit(fromId, toId, fromType.toString(),
        toType.toString(), OperationEnum.REMOVE));
    return auditHistory;
  }

  private static EntityRelationshipAudit getEntityRelationshipAudit(UUID fromId, UUID toId,
      String fromType, String toType, OperationEnum operation) {
    EntityRelationshipAudit audit = new EntityRelationshipAudit();
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

  public static UpdateStateRequest getUpdateStateRequest(String state, String reason) {
    UpdateStateRequest request = new UpdateStateRequest();
    request.setState(UpdateStateRequest.StateEnum.fromValue(state));
    request.setReason(reason);
    return request;
  }

  public static UpdateAssigneeRequest getUpdateAssigneeRequest(String assignee) {
    UpdateAssigneeRequest request = new UpdateAssigneeRequest();
    request.setAssignee(assignee);
    return request;
  }

  public static List<LiveAlertAuditDTO> getAlertAuditDTOs() {
    List<LiveAlertAuditDTO> alertAuditDTOS = new ArrayList<>();
    alertAuditDTOS
        .add(getAlertAuditDTO(UUIDs.timeBased(), "alert 1", "Open", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
            "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
            "Dave Jones", TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(10)));

    return alertAuditDTOS;
  }

  private static LiveAlertAuditDTO getAlertAuditDTO(UUID id, String description, String state,
      Instant startTime, Instant endTime, String type,
      String configuration, String instrumentDescription, Set<String> participants, String title,
      String businessUnit, Set<String> classification, String assignee, Instant when, String who) {
    LiveAlertAuditDTO alertAuditDTO = new LiveAlertAuditDTO();
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
    LiveAlertAuditDTOPrimaryKey alertAuditDTOPrimaryKey = new LiveAlertAuditDTOPrimaryKey();
    alertAuditDTO.setUpdatedBy(who);
    alertAuditDTO.setUpdatedDate(when);
    alertAuditDTO.setRunId(TestUtils.randomUUID());
    alertAuditDTO.setSandboxAlertId(TestUtils.randomUUID());
    alertAuditDTOPrimaryKey.setAlertId(id);
    alertAuditDTOPrimaryKey.setAuditId(TestUtils.randomUUID());
    alertAuditDTO.setPrimaryKey(alertAuditDTOPrimaryKey);

    return alertAuditDTO;
  }

  public static List<LiveAlertAuditDTO> getAlertAuditsWithNulls() {
    List<LiveAlertAuditDTO> alertAudits = new ArrayList<>();
    alertAudits.add(null);
    return alertAudits;
  }

  public static List<AlertAudit> getAlertAudits(UUID alertId) {
    List<AlertAudit> alertAudits = new ArrayList<>();
    alertAudits.add(getAlertAudit(alertId, "alert 1", "Open", TestUtils.randomInstant(),
        TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
        "PO1809 (Palm Olein Future)", Arrays.asList("Eleis Commodities"), "Wash Trade",
        "Europe/Equity", Arrays.asList("Regulatory", "Operational"), "Dave Jones",
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(10)));

    return alertAudits;
  }

  private static AlertAudit getAlertAudit(UUID id, String description, String state,
      Instant startTime,
      Instant endTime, String type,
      String configuration, String instrumentDescription, List<String> participants, String title,
      String businessUnit, List<String> classification, String assignee, Instant when, String who) {
    AlertAudit alertAudit = new AlertAudit();
    alertAudit.setState(AlertAudit.StateEnum.fromValue(state));
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
    alertAudit.setAuditId(TestUtils.randomUUID().toString());
    return alertAudit;
  }
}
