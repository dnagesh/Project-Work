package com.webtech.service.alertconfiguration.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import com.irisium.service.alertconfiguration.model.EntityRelationship;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.entityrelationship.model.Relationship;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RelationshipMapperTest {

  private RelationshipMapper relationshipMapper;

  @Before
  public void setup() {
    relationshipMapper = new RelationshipMapper();
  }

  @Test
  public void transformWhenRequestIsNull() {
    EntityRelationship entityRelationship = relationshipMapper.transform(null);
    assertThat(entityRelationship).isNull();
  }

  @Test
  public void transformWhenRequestWithFromIdAndToIdIsNull() {
    Relationship request = new Relationship();
    EntityRelationship entityRelationship = relationshipMapper.transform(request);
    assertThat(entityRelationship.getFromId()).isNull();
    assertThat(entityRelationship.getToId()).isNull();
  }


  @Test
  public void whenValidDTOThenReturnValidEntityRelationshipApi() {

    Relationship relationship = new Relationship();
    relationship.setFromId(TestUtils.randomUUID());
    relationship.setFromType(EntityType.LIVEALERTCONFIGURATION);
    relationship.setToId(TestUtils.randomUUID());
    relationship.setToType(EntityType.TAG);
    relationship.setUser(TestUtils.randomAlphanumeric(10));
    EntityRelationship result = relationshipMapper.transform(relationship);

    assertThat(result).isNotNull();
    assertThat(result.getFromId()).isEqualTo(relationship.getFromId().toString());
    assertThat(result.getFromType()).isEqualTo(relationship.getFromType().toString());
    assertThat(result.getToId()).isEqualTo(relationship.getToId().toString());
    assertThat(result.getToType()).isEqualTo(relationship.getToType().toString());
    assertThat(result.getWhen()).isEqualTo(relationship.getWhen());
    assertThat(result.getUser()).isEqualTo(relationship.getUser());
  }


}
