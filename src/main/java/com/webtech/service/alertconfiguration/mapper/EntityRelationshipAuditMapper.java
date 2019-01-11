package com.webtech.service.alertconfiguration.mapper;

import com.irisium.service.alertconfiguration.model.EntityRelationshipAudit;
import com.irisium.service.alertconfiguration.model.EntityRelationshipAudit.OperationEnum;
import com.webtech.service.common.Transformer;
import com.webtech.service.entityrelationship.model.RelationshipAudit;
import org.springframework.stereotype.Component;

@Component
public class EntityRelationshipAuditMapper implements
    Transformer<RelationshipAudit, EntityRelationshipAudit> {

  @Override
  public EntityRelationshipAudit transform(RelationshipAudit relationship) {

    EntityRelationshipAudit entityRelationship = null;
    if (relationship != null) {
      entityRelationship = new EntityRelationshipAudit();
      entityRelationship
          .setFromId(relationship.getFromId() != null ? relationship.getFromId().toString() : null);
      entityRelationship.setFromType(
          relationship.getFromType() != null ? relationship.getFromType().toString() : null);
      entityRelationship
          .setToId(relationship.getToId() != null ? relationship.getToId().toString() : null);
      entityRelationship
          .setToType(relationship.getToType() != null ? relationship.getToType().toString() : null);
      entityRelationship.setWhen(relationship.getWhen());
      entityRelationship.setUser(relationship.getOperationWho());
      entityRelationship
          .setOperation(OperationEnum.fromValue(relationship.getRelationshipOperation()));
    }
    return entityRelationship;

  }

}

