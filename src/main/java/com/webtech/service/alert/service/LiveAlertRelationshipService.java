package com.webtech.service.alert.service;

import com.webtech.service.alert.dto.LiveAlertDTO;
import com.webtech.service.alert.mapper.LiveAlertObjectMapper;
import com.webtech.service.alert.repository.LiveAlertRepository;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.entityrelationship.model.Relationship;
import com.webtech.service.entityrelationship.model.RelationshipAudit;
import com.webtech.service.entityrelationship.service.EntityRelationshipService;
import com.irisium.service.livealert.model.EntityLinkRequest;
import com.irisium.service.livealert.model.EntityRelationship;
import com.irisium.service.livealert.model.EntityRelationshipAudit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class LiveAlertRelationshipService {

  private final LiveAlertRepository repository;
  private EntityRelationshipService entityRelationshipService;
  private LiveAlertObjectMapper mapper;

  public LiveAlertRelationshipService(EntityRelationshipService entityRelationshipService,
      LiveAlertRepository repository, LiveAlertObjectMapper mapper) {
    this.entityRelationshipService = entityRelationshipService;
    this.repository = repository;
    this.mapper = mapper;
  }

  public EntityRelationship createRelationship(String alertId, EntityLinkRequest entityLinkRequest,
                                               EntityType fromType, EntityType toType, String username)
      throws EntityNotFoundException {

    LiveAlertDTO alertDTO = repository.findById(UUID.fromString(alertId))
        .orElseThrow(() -> new EntityNotFoundException(EntityType.ALERT.name(), alertId));
    Relationship relationship = entityRelationshipService
        .createEntityRelationship(alertDTO.getAlertId(),
            UUID.fromString(entityLinkRequest.getEntityId()),
            fromType, toType, username);
    return mapper.relationshipToApi(relationship);
  }

  public EntityRelationship deleteRelationship(String alertId, String tagId, EntityType fromType,
      EntityType toType, String username)
      throws EntityNotFoundException {

    LiveAlertDTO alertDTO = repository.findById(UUID.fromString(alertId))
        .orElseThrow(() -> new EntityNotFoundException(EntityType.ALERT.name(), alertId));
    Relationship relationship = entityRelationshipService
        .removeEntityRelationship(alertDTO.getAlertId(), UUID.fromString(tagId),
            fromType, toType, username);
    return mapper.relationshipToApi(relationship);
  }

  public List<String> getRelationshipsByAlertId(String alertId, EntityType entityType) {
    List<Relationship> entityRelationships = entityRelationshipService
        .getRelationships(UUID.fromString(alertId));
    return CollectionUtils.isEmpty(entityRelationships) ? Collections.emptyList()
        : entityRelationships.stream()
            .filter(relationship -> entityType.toString()
                .equals(relationship.getToType().toString()))
            .map(Relationship::getToId).map(Object::toString).collect(
                Collectors.toList());
  }

  public List<EntityRelationshipAudit> getRelationshipAudit(String alertId, String tagId)
      throws EntityNotFoundException {
    LiveAlertDTO alertDTO = repository.findById(UUID.fromString(alertId))
        .orElseThrow(() -> new EntityNotFoundException(EntityType.ALERT.name(), alertId));

    List<RelationshipAudit> relationshipAuditHistory =
        entityRelationshipService
            .getRelationshipAudit(alertDTO.getAlertId(), UUID.fromString(tagId));
    return mapper.mapRelationshipAuditListToApi(relationshipAuditHistory);
  }
}
