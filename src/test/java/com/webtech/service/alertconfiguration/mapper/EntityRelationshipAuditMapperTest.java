package com.webtech.service.alertconfiguration.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import com.irisium.service.alertconfiguration.model.EntityRelationshipAudit;
import com.irisium.service.alertconfiguration.model.EntityRelationshipAudit.OperationEnum;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.entityrelationship.model.RelationshipAudit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EntityRelationshipAuditMapperTest {

  private EntityRelationshipAuditMapper relationshipAuditMapper;

  @Before
  public void setup() {
    relationshipAuditMapper = new EntityRelationshipAuditMapper();
  }

  @Test
  public void transformWhenRequestIsNull() {
    EntityRelationshipAudit entityRelationshipAudit = relationshipAuditMapper.transform(null);
    assertThat(entityRelationshipAudit).isNull();
  }

  @Test
  public void transformWhenRequestWithFromIdAndToIdIsNull() {
    RelationshipAudit request = new RelationshipAudit();
    EntityRelationshipAudit entityRelationshipAudit = relationshipAuditMapper.transform(request);
    assertThat(entityRelationshipAudit.getFromId()).isNull();
    assertThat(entityRelationshipAudit.getToId()).isNull();
  }


  @Test
  public void transformWhenValidDTOThenReturnValidEntityRelationshipApi() {

    RelationshipAudit relationshipAudit = new RelationshipAudit();
    relationshipAudit.setFromId(TestUtils.randomUUID());
    relationshipAudit.setFromType(EntityType.LIVEALERTCONFIGURATION);
    relationshipAudit.setToId(TestUtils.randomUUID());
    relationshipAudit.setToType(EntityType.TAG);
    relationshipAudit.setOperationWho(TestUtils.randomAlphanumeric(10));
    relationshipAudit.setRelationshipOperation(OperationEnum.ADD.toString());
    EntityRelationshipAudit result = relationshipAuditMapper.transform(relationshipAudit);

    assertThat(result).isNotNull();
    assertThat(result.getFromId()).isEqualTo(relationshipAudit.getFromId().toString());
    assertThat(result.getFromType()).isEqualTo(relationshipAudit.getFromType().toString());
    assertThat(result.getToId()).isEqualTo(relationshipAudit.getToId().toString());
    assertThat(result.getToType()).isEqualTo(relationshipAudit.getToType().toString());
    assertThat(result.getWhen()).isEqualTo(relationshipAudit.getWhen());
    assertThat(result.getUser()).isEqualTo(relationshipAudit.getOperationWho());
    assertThat(result.getOperation().toString())
        .isEqualTo(relationshipAudit.getRelationshipOperation());
  }

}
