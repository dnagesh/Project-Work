package com.webtech.service.alert.service;

import com.webtech.service.alert.dto.SandboxAlertDTO;
import com.webtech.service.alert.dto.SandboxAlertDTOPrimaryKey;
import com.webtech.service.alert.mapper.SandboxAlertObjectMapper;
import com.webtech.service.alert.repository.SandboxAlertDTORepository;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.entityrelationship.model.Relationship;
import com.webtech.service.entityrelationship.model.RelationshipAudit;
import com.webtech.service.entityrelationship.service.EntityRelationshipService;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityLinkRequest;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityRelationship;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityRelationshipAudit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class SandboxAlertRelationshipService {

  private static final String SANDBOX_ALERT_ID = "(SandboxAlertId: %s, runId: %s )";
  private final SandboxAlertDTORepository sandboxAlertDTORepository;
  private EntityRelationshipService entityRelationshipService;
  private SandboxAlertObjectMapper mapper;

  public SandboxAlertRelationshipService(EntityRelationshipService entityRelationshipService,
      SandboxAlertDTORepository sandboxAlertDTORepository, SandboxAlertObjectMapper mapper) {
    this.entityRelationshipService = entityRelationshipService;
    this.sandboxAlertDTORepository = sandboxAlertDTORepository;
    this.mapper = mapper;
  }

  public SandboxAlertEntityRelationship createRelationship(String sandboxAlertId, String runId,
                                                           SandboxAlertEntityLinkRequest entityLinkRequest,
                                                           EntityType fromType, EntityType toType, String username)
      throws EntityNotFoundException {

    SandboxAlertDTO alertDTO = sandboxAlertDTORepository.findById(new SandboxAlertDTOPrimaryKey(
        UUID.fromString(runId), UUID.fromString(sandboxAlertId))).orElseThrow(
        () -> new EntityNotFoundException(EntityType.SANDBOXALERT.name(), String.format(
            SANDBOX_ALERT_ID, sandboxAlertId, runId)));

    Relationship relationship = entityRelationshipService
        .createEntityRelationship(alertDTO.getPrimaryKey().getAlertId(),
            UUID.fromString(entityLinkRequest.getEntityId()),
            fromType, toType, username);
    return mapper.relationshipToApi(relationship);
  }

  public SandboxAlertEntityRelationship deleteRelationship(String sandboxAlertId, String runId,
      String tagId, EntityType fromType, EntityType toType, String username)
      throws EntityNotFoundException {

    SandboxAlertDTO alertDTO = sandboxAlertDTORepository.findById(new SandboxAlertDTOPrimaryKey(
        UUID.fromString(runId), UUID.fromString(sandboxAlertId))).orElseThrow(
        () -> new EntityNotFoundException(EntityType.SANDBOXALERT.name(), String.format(
            SANDBOX_ALERT_ID, sandboxAlertId, runId)));

    Relationship relationship = entityRelationshipService
        .removeEntityRelationship(alertDTO.getPrimaryKey().getAlertId(), UUID.fromString(tagId),
            fromType, toType, username);
    return mapper.relationshipToApi(relationship);
  }

  public List<String> getRelationshipsByAlertId(String sandboxAlertId, String runId,
      EntityType entityType)
      throws EntityNotFoundException {
    SandboxAlertDTO alertDTO = sandboxAlertDTORepository.findById(new SandboxAlertDTOPrimaryKey(
        UUID.fromString(runId), UUID.fromString(sandboxAlertId))).orElseThrow(
        () -> new EntityNotFoundException(EntityType.SANDBOXALERT.name(), String.format(
            SANDBOX_ALERT_ID, sandboxAlertId, runId)));

    List<Relationship> entityRelationships = entityRelationshipService
        .getRelationships(alertDTO.getPrimaryKey().getAlertId());
    return CollectionUtils.isEmpty(entityRelationships) ? Collections.emptyList()
        : entityRelationships.stream()
            .filter(relationship -> entityType.toString()
                .equals(relationship.getToType().toString()))
            .map(Relationship::getToId).map(Object::toString).collect(
                Collectors.toList());
  }

  public List<SandboxAlertEntityRelationshipAudit> getRelationshipAudit(String sandboxAlertId,
      String runId, String tagId)
      throws EntityNotFoundException {
    SandboxAlertDTO alertDTO = sandboxAlertDTORepository.findById(new SandboxAlertDTOPrimaryKey(
        UUID.fromString(runId), UUID.fromString(sandboxAlertId))).orElseThrow(
        () -> new EntityNotFoundException(EntityType.SANDBOXALERT.name(), String.format(
            SANDBOX_ALERT_ID, sandboxAlertId, runId)));

    List<RelationshipAudit> relationshipAuditHistory =
        entityRelationshipService
            .getRelationshipAudit(alertDTO.getPrimaryKey().getAlertId(), UUID.fromString(tagId));
    return mapper.mapRelationshipAuditListToApi(relationshipAuditHistory);
  }
}
