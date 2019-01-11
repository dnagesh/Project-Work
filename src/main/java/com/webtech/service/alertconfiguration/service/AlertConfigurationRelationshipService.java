package com.webtech.service.alertconfiguration.service;


import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTOPrimaryKey;
import com.webtech.service.alertconfiguration.exception.AlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.mapper.EntityRelationshipAuditMapper;
import com.webtech.service.alertconfiguration.mapper.RelationshipMapper;
import com.irisium.service.alertconfiguration.model.EntityLinkRequest;
import com.irisium.service.alertconfiguration.model.EntityRelationship;
import com.irisium.service.alertconfiguration.model.EntityRelationshipAudit;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationRepository;
import com.webtech.service.alertconfiguration.repository.SandboxAlertConfigurationRepository;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.entityrelationship.model.Relationship;
import com.webtech.service.entityrelationship.model.RelationshipAudit;
import com.webtech.service.entityrelationship.service.EntityRelationshipService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class AlertConfigurationRelationshipService {

  private final LiveAlertConfigurationRepository liveAlertRepository;
  private final SandboxAlertConfigurationRepository sandboxAlertRepository;
  private final EntityRelationshipService entityRelationshipService;
  private final RelationshipMapper relationshipMapper;
  private final EntityRelationshipAuditMapper relationshipAuditMapper;

  public AlertConfigurationRelationshipService(EntityRelationshipService entityRelationshipService,
      LiveAlertConfigurationRepository liveAlertRepository,
      SandboxAlertConfigurationRepository sandboxAlertRepository,
      RelationshipMapper relationshipMapper, EntityRelationshipAuditMapper relationshipAuditMapper
  ) {
    this.entityRelationshipService = entityRelationshipService;
    this.liveAlertRepository = liveAlertRepository;
    this.sandboxAlertRepository = sandboxAlertRepository;
    this.relationshipMapper = relationshipMapper;
    this.relationshipAuditMapper = relationshipAuditMapper;
  }

  /**
   * @param alertConfigurationId - id of the either sandbox or live alert configuration
   * @param entityLinkRequest - this has tag data
   * @param fromType either LIVEALERTCONFIGURATION or SANDBOXALERTCONFIGURATION
   * @param toType TAG
   * @param username who creates this tag
   * @param sandboxId - sandbox id which is used to decide validate the given  alertConfigurationId
   * @return EntityRelationship
   */
  public EntityRelationship createRelationship(String alertConfigurationId,
                                               EntityLinkRequest entityLinkRequest,
                                               EntityType fromType, EntityType toType, String username, String sandboxId)
      throws AlertConfigurationNotFoundException {
    UUID alertConfigUUID = getAlertConfigurationId(sandboxId,
        alertConfigurationId);
    Relationship relationship = entityRelationshipService
        .createEntityRelationship(alertConfigUUID,
            UUID.fromString(entityLinkRequest.getEntityId()),
            fromType, toType, username);
    return relationshipMapper.transform(relationship);
  }

  /**
   * @param alertConfigurationId id of the either sandbox or live alert configuration
   * @param entityId Tag Id
   * @param fromType either LIVEALERTCONFIGURATION or SANDBOXALERTCONFIGURATION
   * @param toType TAG
   * @param username who deletes this tag
   * @param sandboxId sandbox id which is used to validate the given  alertConfigurationId
   * @return EntityRelationship
   */
  public EntityRelationship deleteRelationship(String alertConfigurationId, String entityId,
      EntityType fromType,
      EntityType toType, String username, String sandboxId)
      throws AlertConfigurationNotFoundException {

    UUID alertConfigUUID = getAlertConfigurationId(sandboxId, alertConfigurationId);
    Relationship relationship = entityRelationshipService
        .removeEntityRelationship(alertConfigUUID, UUID.fromString(entityId),
            fromType, toType, username);
    return relationshipMapper.transform(relationship);
  }

  public List<String> getRelationshipsByAlertConfigurationId(String alertConfigurationId,
      EntityType entityType) {
    List<Relationship> entityRelationships = entityRelationshipService
        .getRelationships(UUID.fromString(alertConfigurationId));
    return CollectionUtils.isEmpty(entityRelationships) ? Collections.emptyList()
        : entityRelationships.stream().filter(relationship -> entityType.toString()
            .equals(relationship.getToType().toString()))
            .map(Relationship::getToId).map(Object::toString).collect(Collectors.toList());
  }

  /**
   * @param alertConfigurationId id of the either sandbox or live alert configuration
   * @param entityId Tag Id
   * @param sandboxId sandbox id which is used to validate the given  alertConfigurationId
   * @return List<EntityRelationshipAudit>
   */
  public List<EntityRelationshipAudit> getRelationshipAudit(String alertConfigurationId,
      String entityId, String sandboxId) throws AlertConfigurationNotFoundException {
    UUID alertConfigUUID = getAlertConfigurationId(sandboxId, alertConfigurationId);
    List<RelationshipAudit> relationshipAuditHistory = entityRelationshipService
        .getRelationshipAudit(alertConfigUUID, UUID.fromString(entityId));
    return relationshipAuditHistory.isEmpty() ? Collections.emptyList()
        : relationshipAuditHistory.stream().map(relationshipAuditMapper::transform)
            .collect(Collectors.toList());
  }

  private UUID getAlertConfigurationId(String sandboxId, String alertConfigurationId)
      throws AlertConfigurationNotFoundException {

    UUID alertConfigUUID;

    if (StringUtils.isEmpty(sandboxId)) {
      LiveAlertConfigurationDTO alertConfigurationDTO = liveAlertRepository
          .findById(UUID.fromString(alertConfigurationId))
          .orElseThrow(() -> new AlertConfigurationNotFoundException(alertConfigurationId));
      alertConfigUUID = alertConfigurationDTO.getUuid();
    } else {
      SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey();
      primaryKey.setSandboxUUID(UUID.fromString(sandboxId));
      primaryKey.setAlertConfigurationUUID(UUID.fromString(alertConfigurationId));
      SandboxAlertConfigurationDTO alertConfigurationDTO = sandboxAlertRepository
          .findById(primaryKey)
          .orElseThrow(() -> new AlertConfigurationNotFoundException(alertConfigurationId));
      alertConfigUUID = alertConfigurationDTO.getPrimaryKey().getAlertConfigurationUUID();
    }

    return alertConfigUUID;

  }

}
