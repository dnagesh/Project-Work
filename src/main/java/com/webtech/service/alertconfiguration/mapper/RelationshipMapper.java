package com.webtech.service.alertconfiguration.mapper;

import com.irisium.service.alertconfiguration.model.EntityRelationship;
import com.webtech.service.common.Transformer;
import com.webtech.service.entityrelationship.model.Relationship;
import org.springframework.stereotype.Component;

@Component
public class RelationshipMapper implements Transformer<Relationship, EntityRelationship> {

  @Override
  public EntityRelationship transform(Relationship relationship) {

    EntityRelationship entityRelationship = null;
    if (relationship != null) {
      entityRelationship = new EntityRelationship();
      entityRelationship
          .setFromId(relationship.getFromId() != null ? relationship.getFromId().toString() : null);
      entityRelationship.setFromType(
          relationship.getFromType() != null ? relationship.getFromType().toString() : null);
      entityRelationship
          .setToId(relationship.getToId() != null ? relationship.getToId().toString() : null);
      entityRelationship
          .setToType(relationship.getToType() != null ? relationship.getToType().toString() : null);
      entityRelationship.setWhen(relationship.getWhen());
      entityRelationship.setUser(relationship.getUser());
    }
    return entityRelationship;
  }

}

