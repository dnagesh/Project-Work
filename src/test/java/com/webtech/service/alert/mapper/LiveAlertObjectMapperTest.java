package com.webtech.service.alert.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import com.webtech.service.alert.LiveAlertTestObjects;
import com.webtech.service.alert.dto.LiveAlertAuditDTO;
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
import com.irisium.service.livealert.model.EntityRelationship;
import com.irisium.service.livealert.model.EntityRelationshipAudit;
import com.irisium.service.livealert.model.EntityRelationshipAudit.OperationEnum;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LiveAlertObjectMapperTest {

  private LiveAlertObjectMapper mapper;
  private LiveAlertDTO dto;
  private CreateAlertRequest request;
  private Relationship relationship;


  @Before
  public void setup() {
    mapper = new LiveAlertObjectMapper();
    UUID alertId = TestUtils.randomUUID();

    dto = LiveAlertTestObjects.getAlertDTO(alertId, "alert 1", "OPEN", TestUtils.randomInstant(),
        TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
        "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
        "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
        "Dave Jones");

    request = LiveAlertTestObjects
        .getAlertCreateRequest("alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", Arrays.asList("Eleis Commodities"), "Wash Trade",
            "Europe/Equity", Arrays.asList("Regulatory", "Operational"));

    relationship = LiveAlertTestObjects
        .getRelationship(TestUtils.randomUUID(), TestUtils.randomUUID(), EntityType.ALERT,
            EntityType.TAG, TestUtils.randomAlphanumeric(10));
  }

  @Test
  public void shouldReturnEmptyApiWhenEmptyDTO() {
    Alert result = mapper.dtoToApi(null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldReturnApiWhenDTOExists() {

    LiveAlertDTO alertDTO = new LiveAlertDTO();
    alertDTO.setAlertId(TestUtils.randomUUID());
    Alert result = mapper.dtoToApi(alertDTO);
    assertThat(result).isNotNull();
  }

  @Test
  public void shouldReturnValidApiWhenValidDTO() {
    Alert result = mapper.dtoToApi(dto);

    assertThat(result).isNotNull();
    assertThat(result.getAlertId()).isEqualTo(dto.getAlertId().toString());
    assertThat(result.getConfiguration()).isEqualTo(dto.getConfiguration());
    assertThat(result.getType()).isEqualTo(dto.getType());
    assertThat(result.getState().toString()).isEqualTo(dto.getState());
    assertThat(result.getDescription()).isEqualTo(dto.getDescription());
    assertThat(result.getEndTime()).isEqualTo(dto.getEndTime());
    assertThat(result.getStartTime()).isEqualTo(dto.getStartTime());
    assertThat(result.getInstrumentDescription()).isEqualTo(dto.getInstrumentDescription());
    assertThat(result.getParticipants().get(0)).isEqualTo(dto.getParticipants().iterator().next());
    assertThat(result.getTitle()).isEqualTo(request.getTitle());
    assertThat(result.getBusinessUnit()).isEqualTo(request.getBusinessUnit());
    assertThat(result.getClassification()).hasSameSizeAs(request.getClassification());

    assertThat(result.getApsHash()).isEqualTo(dto.getApsHash());
    assertThat(result.getRunId()).isEqualTo(dto.getRunId().toString());
    assertThat(result.getSandboxAlertId()).isEqualTo(dto.getSandboxAlertId().toString());
    assertThat(result.getCreatedDate()).isEqualTo(dto.getCreatedDate());
    assertThat(result.getUpdatedDate()).isEqualTo(dto.getUpdatedDate());
    assertThat(result.getUpdatedBy()).isEqualTo(dto.getUpdatedBy());

  }

  @Test
  public void shouldReturnEmptyDtoWhenEmptyRequest() {
    LiveAlertDTO result = mapper.requestToDto(null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldReturnValidDtoWhenValidRequestFromLive() {
    LiveAlertDTO result = mapper.requestToDto(request);
    assertThat(result).isNotNull();

    assertThat(result.getConfiguration()).isEqualTo(request.getConfiguration());
    assertThat(result.getType()).isEqualTo(request.getType());
    assertThat(result.getState()).isEqualTo(request.getState().toString());
    assertThat(result.getDescription()).isEqualTo(request.getDescription());
    assertThat(result.getStartTime()).isEqualTo(request.getStartTime());
    assertThat(result.getEndTime()).isEqualTo(request.getEndTime());
    assertThat(result.getInstrumentDescription()).isEqualTo(request.getInstrumentDescription());
    assertThat(result.getParticipants().iterator().next())
        .isEqualTo(request.getParticipants().iterator().next());
    assertThat(result.getTitle()).isEqualTo(request.getTitle());
    assertThat(result.getBusinessUnit()).isEqualTo(request.getBusinessUnit());
    assertThat(result.getClassification()).hasSameSizeAs(request.getClassification());

    assertThat(result.getApsHash()).isEqualTo(request.getApsHash());
    assertThat(result.getRunId()).isNull();
    assertThat(result.getSandboxAlertId()).isNull();
    assertThat(result.getCreatedDate()).isNotNull();

  }

  @Test
  public void shouldReturnValidDtoWhenValidRequestFromSandboxAlertPromotion() {
    request.setRunId(TestUtils.randomUUID().toString());
    request.setSandboxAlertId(TestUtils.randomUUID().toString());

    LiveAlertDTO result = mapper.requestToDto(request);
    assertThat(result).isNotNull();

    assertThat(result.getConfiguration()).isEqualTo(request.getConfiguration());
    assertThat(result.getType()).isEqualTo(request.getType());
    assertThat(result.getState()).isEqualTo(request.getState().toString());
    assertThat(result.getDescription()).isEqualTo(request.getDescription());
    assertThat(result.getStartTime()).isEqualTo(request.getStartTime());
    assertThat(result.getEndTime()).isEqualTo(request.getEndTime());
    assertThat(result.getInstrumentDescription()).isEqualTo(request.getInstrumentDescription());
    assertThat(result.getParticipants().iterator().next())
        .isEqualTo(request.getParticipants().iterator().next());
    assertThat(result.getTitle()).isEqualTo(request.getTitle());
    assertThat(result.getBusinessUnit()).isEqualTo(request.getBusinessUnit());
    assertThat(result.getClassification()).hasSameSizeAs(request.getClassification());

    assertThat(result.getApsHash()).isEqualTo(request.getApsHash());
    assertThat(result.getCreatedDate()).isNotNull();

    assertThat(result.getRunId()).isNotNull();
    assertThat(result.getSandboxAlertId()).isNotNull();
    assertThat(result.getRunId().toString()).isEqualTo(request.getRunId());
    assertThat(result.getSandboxAlertId().toString()).isEqualTo(request.getSandboxAlertId());

  }


  @Test
  public void shouldReturnEmptyOutputWhenEmptyInput() {
    List<Alert> result = mapper.mapList(Collections.EMPTY_LIST);

    assertThat(result).hasSize(0);
  }

  @Test
  public void shouldReturnValidOutputWhenValidInput() {

    List<LiveAlertDTO> dtoList = LiveAlertTestObjects.getAlertDTOList();
    List<Alert> result = mapper.mapList(dtoList);

    assertThat(result).hasSize(dtoList.size());
    assertThat(result.get(0).getAlertId()).isEqualTo(dtoList.get(0).getAlertId().toString());
  }

  @Test
  public void shouldReturnCommentDtoWhenValidCommentRequest() {
    CommentCreateRequest commentCreateRequest = LiveAlertTestObjects.getCommentCreateRequest();
    LiveAlertCommentDTO result = mapper.commentRequestToDto(commentCreateRequest, "User1");

    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("User1");
    assertThat(result.getComment()).isEqualTo(commentCreateRequest.getComment());
    assertThat(result.getCreationTime()).isNull();
    assertThat(result.getAlertId()).isNull();
    assertThat(result.getCommentId()).isNull();
  }

  @Test
  public void shouldNotReturnCommentDtoWhenInvalidCommentRequest() {
    LiveAlertCommentDTO result = mapper.commentRequestToDto(null, null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldReturnCommentApiWhenValidCommentDto() {
    LiveAlertCommentDTO commentDto = LiveAlertTestObjects
        .getCommentDTO(TestUtils.randomUUID(), TestUtils.randomUUID(), "User1", "Comment1",
            TestUtils.randomInstant());
    Comment result = mapper.commentDtoToApi(commentDto);

    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo(commentDto.getUsername());
    assertThat(result.getComment()).isEqualTo(commentDto.getComment());
    assertThat(result.getCreationTime()).isEqualTo(commentDto.getCreationTime());
    assertThat(result.getAlertId()).isEqualTo(commentDto.getAlertId().toString());
    assertThat(result.getCommentId()).isEqualTo(commentDto.getCommentId().toString());
  }

  @Test
  public void shouldNotReturnCommentApiWhenInvalidCommentDto() {
    Comment comment = mapper.commentDtoToApi(null);
    assertThat(comment).isNull();
  }

  @Test
  public void shouldReturnCommentApiListWhenValidCommentDtoList() {
    List<LiveAlertCommentDTO> dtoList = LiveAlertTestObjects.getCommentDTOList();
    List<Comment> result = mapper.mapCommentsList(dtoList);

    assertThat(result).isNotNull();
    assertThat(result).hasSize(dtoList.size());
  }

  @Test
  public void shouldNotReturnCommentApiListWhenInvalidCommentDtoList() {
    List<Comment> result = mapper.mapCommentsList(null);
    assertThat(result).isEmpty();
  }

  @Test
  public void shouldNotReturnRelationshipApiWhenInvalidRelationshipDto() {
    EntityRelationship result = mapper.relationshipToApi(null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldReturnRelationshipApiWhenValidRelationshipDto() {
    EntityRelationship result = mapper.relationshipToApi(relationship);

    assertThat(result).isNotNull();
    assertThat(result.getFromId()).isEqualTo(relationship.getFromId().toString());
    assertThat(result.getFromType()).isEqualTo(relationship.getFromType().toString());
    assertThat(result.getToId()).isEqualTo(relationship.getToId().toString());
    assertThat(result.getToType()).isEqualTo(relationship.getToType().toString());
    assertThat(result.getWhen()).isEqualTo(relationship.getWhen());
    assertThat(result.getUser()).isEqualTo(relationship.getUser());
  }

  @Test
  public void shouldReturnEmptyApiListForEmptyOrNullRelationshipAuditModelList() {
    List<EntityRelationshipAudit> auditList = mapper.mapRelationshipAuditListToApi(null);
    assertThat(auditList).isNotNull();
    assertThat(auditList).isEmpty();

    auditList = mapper.mapRelationshipAuditListToApi(Collections.emptyList());
    assertThat(auditList).isNotNull();
    assertThat(auditList).isEmpty();

    List<RelationshipAudit> relationshipAudits = LiveAlertTestObjects
        .getRelationshipAuditsWithNulls();
    auditList = mapper.mapRelationshipAuditListToApi(relationshipAudits);
    assertThat(auditList).isNotNull();
    assertThat(auditList).isNotEmpty();
    assertThat(auditList.get(0)).isNull();

  }

  @Test
  public void shouldReturnValidApiListForValidRelationshipAuditModelList() {
    UUID alertId = TestUtils.randomUUID();
    UUID entityId = TestUtils.randomUUID();
    List<RelationshipAudit> relationshipAudits = LiveAlertTestObjects
        .getRelationshipAudits(alertId, entityId, EntityType.ALERT,
            EntityType.TAG);
    List<EntityRelationshipAudit> auditList = mapper
        .mapRelationshipAuditListToApi(relationshipAudits);
    assertThat(auditList).isNotNull();
    assertThat(auditList).hasSize(relationshipAudits.size());
    assertThat(auditList.get(0).getFromId()).isEqualTo(alertId.toString());
    assertThat(auditList.get(0).getToId()).isEqualTo(entityId.toString());
    assertThat(auditList.get(0).getOperation()).isEqualTo(OperationEnum.ADD);
    assertThat(auditList.get(1).getFromId()).isEqualTo(alertId.toString());
    assertThat(auditList.get(1).getToId()).isEqualTo(entityId.toString());
    assertThat(auditList.get(1).getOperation()).isEqualTo(OperationEnum.REMOVE);
  }

  @Test
  public void shouldReturnEmptyAuditDTOForInvalidAlertDTO() {
    LiveAlertAuditDTO auditDTO = mapper
        .alertDTOToAlertAuditDTO(null);
    assertThat(auditDTO).isNull();
  }


  @Test
  public void shouldReturnValidAuditDTOForValidAlertDTO() {
    LiveAlertAuditDTO result = mapper.alertDTOToAlertAuditDTO(dto);
    assertThat(result).isNotNull();
    assertThat(result.getPrimaryKey()).isNotNull();
    assertThat(result.getPrimaryKey().getAlertId()).isEqualTo(dto.getAlertId());
    assertThat(result.getPrimaryKey().getAuditId()).isNotNull();
    assertThat(result.getCreatedDate()).isEqualTo(dto.getCreatedDate());
    assertThat(result.getUpdatedBy()).isEqualTo(dto.getUpdatedBy());
    assertThat(result.getUpdatedDate()).isEqualTo(dto.getUpdatedDate());

    assertThat(result.getConfiguration()).isEqualTo(dto.getConfiguration());
    assertThat(result.getType()).isEqualTo(dto.getType());
    assertThat(result.getState()).isEqualTo(dto.getState());
    assertThat(result.getDescription()).isEqualTo(dto.getDescription());
    assertThat(result.getStartTime()).isEqualTo(dto.getStartTime());
    assertThat(result.getEndTime()).isEqualTo(dto.getEndTime());
    assertThat(result.getInstrumentDescription()).isEqualTo(dto.getInstrumentDescription());
    assertThat(result.getParticipants().iterator().next())
        .isEqualTo(dto.getParticipants().iterator().next());
    assertThat(result.getTitle()).isEqualTo(dto.getTitle());
    assertThat(result.getBusinessUnit()).isEqualTo(dto.getBusinessUnit());
    assertThat(result.getClassification()).hasSameSizeAs(dto.getClassification());

    assertThat(result.getApsHash()).isEqualTo(dto.getApsHash());
    assertThat(result.getRunId()).isEqualTo(dto.getRunId());
    assertThat(result.getSandboxAlertId()).isEqualTo(dto.getSandboxAlertId());
  }

  @Test
  public void shouldReturnEmptyApiListForEmptyOrNullAuditDtoList() {
    List<AlertAudit> alertAudits = mapper.mapAuditDTOListToApiList(null);
    assertThat(alertAudits).isNotNull();
    assertThat(alertAudits).isEmpty();

    alertAudits = mapper.mapAuditDTOListToApiList(Collections.emptyList());
    assertThat(alertAudits).isNotNull();
    assertThat(alertAudits).isEmpty();

    List<LiveAlertAuditDTO> audits = LiveAlertTestObjects.getAlertAuditsWithNulls();
    alertAudits = mapper.mapAuditDTOListToApiList(audits);
    assertThat(alertAudits).isNotNull();
    assertThat(alertAudits).isNotEmpty();
    assertThat(alertAudits.get(0)).isNull();
    assertThat(alertAudits).hasSize(audits.size());

  }


  @Test
  public void shouldReturnValidApiListForValidAlertAuditDtoList() {
    List<LiveAlertAuditDTO> auditDTOS = LiveAlertTestObjects.getAlertAuditDTOs();
    List<AlertAudit> alertAudits = mapper.mapAuditDTOListToApiList(auditDTOS);
    assertThat(alertAudits).isNotNull();
    assertThat(alertAudits).hasSize(auditDTOS.size());
    assertThat(alertAudits.get(0).getDescription()).isEqualTo(auditDTOS.get(0).getDescription());
    assertThat(alertAudits.get(0).getEndTime()).isEqualTo(auditDTOS.get(0).getEndTime());
    assertThat(alertAudits.get(0).getStartTime()).isEqualTo(auditDTOS.get(0).getStartTime());
    assertThat(alertAudits.get(0).getInstrumentDescription())
        .isEqualTo(auditDTOS.get(0).getInstrumentDescription());
    assertThat(alertAudits.get(0).getParticipants().get(0))
        .isEqualTo(auditDTOS.get(0).getParticipants().iterator().next());
    assertThat(alertAudits.get(0).getType()).isEqualTo(auditDTOS.get(0).getType());
    assertThat(alertAudits.get(0).getConfiguration())
        .isEqualTo(auditDTOS.get(0).getConfiguration());
    assertThat(alertAudits.get(0).getTitle()).isEqualTo(auditDTOS.get(0).getTitle());
    assertThat(alertAudits.get(0).getBusinessUnit()).isEqualTo(auditDTOS.get(0).getBusinessUnit());
    assertThat(alertAudits.get(0).getClassification().get(0))
        .isEqualTo(auditDTOS.get(0).getClassification().iterator().next());
    assertThat(alertAudits.get(0).getAssignee()).isEqualTo(auditDTOS.get(0).getAssignee());
    assertThat(alertAudits.get(0).getAlertId())
        .isEqualTo(auditDTOS.get(0).getPrimaryKey().getAlertId().toString());
    assertThat(alertAudits.get(0).getAuditId())
        .isEqualTo(auditDTOS.get(0).getPrimaryKey().getAuditId().toString());
    assertThat(alertAudits.get(0).getCreatedDate())
        .isEqualTo(auditDTOS.get(0).getCreatedDate());
    assertThat(alertAudits.get(0).getUpdatedDate())
        .isEqualTo(auditDTOS.get(0).getUpdatedDate());
    assertThat(alertAudits.get(0).getUpdatedBy())
        .isEqualTo(auditDTOS.get(0).getUpdatedBy());
    assertThat(alertAudits.get(0).getRunId()).isEqualTo(auditDTOS.get(0).getRunId().toString());
    assertThat(alertAudits.get(0).getSandboxAlertId())
        .isEqualTo(auditDTOS.get(0).getSandboxAlertId().toString());

  }

  @Test
  public void shouldReturnValidApiListForValidAlertAuditDtoListWithoutListsPopulated() {
    List<LiveAlertAuditDTO> auditDTOS = LiveAlertTestObjects.getAlertAuditDTOs();
    auditDTOS.get(0).setParticipants(null);
    auditDTOS.get(0).setClassification(null);
    List<AlertAudit> alertAudits = mapper.mapAuditDTOListToApiList(auditDTOS);
    assertThat(alertAudits).isNotNull();
    assertThat(alertAudits).hasSize(auditDTOS.size());
    assertThat(alertAudits.get(0).getParticipants()).isNullOrEmpty();
    assertThat(alertAudits.get(0).getClassification()).isNullOrEmpty();
  }
}
