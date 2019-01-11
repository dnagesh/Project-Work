package com.webtech.service.alert.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import com.webtech.service.alert.SandboxAlertTestObjects;
import com.webtech.service.alert.dto.SandboxAlertAuditDTO;
import com.webtech.service.alert.dto.SandboxAlertCommentDTO;
import com.webtech.service.alert.dto.SandboxAlertDTO;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.entityrelationship.model.Relationship;
import com.webtech.service.entityrelationship.model.RelationshipAudit;
import com.irisium.service.sandboxalert.model.CreateSandboxAlertRequest;
import com.irisium.service.sandboxalert.model.SandboxAlert;
import com.irisium.service.sandboxalert.model.SandboxAlertAudit;
import com.irisium.service.sandboxalert.model.SandboxAlertComment;
import com.irisium.service.sandboxalert.model.SandboxAlertCommentCreateRequest;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityRelationship;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityRelationshipAudit;
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
public class SandboxAlertObjectMapperTest {

  private static final String USER = "User";
  private SandboxAlertObjectMapper mapper;
  private SandboxAlert alert;
  private SandboxAlertDTO dto;
  private CreateSandboxAlertRequest request;
  private Relationship relationship;


  @Before
  public void setup() {
    mapper = new SandboxAlertObjectMapper();
    UUID alertId = TestUtils.randomUUID();
    UUID runId = TestUtils.randomUUID();

    alert = SandboxAlertTestObjects
        .getAlert(alertId.toString(), runId.toString(), "alert 1", "OPEN",
            TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", Arrays.asList("Eleis Commodities"), "Wash Trade",
            "Europe/Equity", Arrays.asList("Regulatory", "Operational"));

    dto = SandboxAlertTestObjects
        .getAlertDTO(alertId, runId, "alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
            "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
            "Dave Jones");

    request = SandboxAlertTestObjects
        .getAlertCreateRequest(runId.toString(), "alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", Arrays.asList("Eleis Commodities"), "Wash Trade",
            "Europe/Equity", Arrays.asList("Regulatory", "Operational"));
    request.setRunId(runId.toString());

    relationship = SandboxAlertTestObjects
        .getRelationship(alertId, TestUtils.randomUUID(), EntityType.SANDBOXALERT,
            EntityType.TAG, TestUtils.randomAlphanumeric(10));
  }


  @Test
  public void shouldReturnEmptyApiWhenEmptyDTO() {
    SandboxAlert result = mapper.dtoToApi(null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldReturnValidApiWhenValidDTO() {
    SandboxAlert result = mapper.dtoToApi(dto);

    assertThat(result).isNotNull();
    assertThat(result.getAlertId()).isEqualTo(dto.getPrimaryKey().getAlertId().toString());
    assertThat(result.getRunId()).isEqualTo(dto.getPrimaryKey().getRunId().toString());
    assertThat(result.getConfiguration()).isEqualTo(dto.getConfiguration());
    assertThat(result.getType()).isEqualTo(dto.getType());
    assertThat(result.getState().toString()).isEqualTo(dto.getState());
    assertThat(result.getDescription()).isEqualTo(dto.getDescription());
    assertThat(result.getEndTime()).isEqualTo(dto.getEndTime());
    assertThat(result.getStartTime()).isEqualTo(dto.getStartTime());
    assertThat(result.getInstrumentDescription()).isEqualTo(dto.getInstrumentDescription());
    assertThat(result.getParticipants().get(0)).isEqualTo(dto.getParticipants().iterator().next());
    assertThat(result.getTitle()).isEqualTo(dto.getTitle());
    assertThat(result.getBusinessUnit()).isEqualTo(dto.getBusinessUnit());
    assertThat(result.getClassification()).hasSameSizeAs(dto.getClassification());
    assertThat(result.isPromotedToLive()).isEqualTo(dto.isPromotedToLive());
    assertThat(result.getApsHash()).isEqualTo(dto.getApsHash());
    assertThat(result.getAssignee()).isEqualTo(dto.getAssignee());
    assertThat(result.isPromotedToLive()).isEqualTo(dto.isPromotedToLive());
  }

  @Test
  public void shouldReturnApiWithEmptyListsWhenValidDTOWithEmptyLists() {
    dto.setClassification(Collections.emptySet());
    dto.setParticipants(null);
    dto.setPromotedToLive(false);
    SandboxAlert result = mapper.dtoToApi(dto);

    assertThat(result).isNotNull();
    assertThat(result.getAlertId()).isEqualTo(dto.getPrimaryKey().getAlertId().toString());
    assertThat(result.getRunId()).isEqualTo(dto.getPrimaryKey().getRunId().toString());
    assertThat(result.getConfiguration()).isEqualTo(dto.getConfiguration());
    assertThat(result.getType()).isEqualTo(dto.getType());
    assertThat(result.getState().toString()).isEqualTo(dto.getState());
    assertThat(result.getDescription()).isEqualTo(dto.getDescription());
    assertThat(result.getEndTime()).isEqualTo(dto.getEndTime());
    assertThat(result.getStartTime()).isEqualTo(dto.getStartTime());
    assertThat(result.getInstrumentDescription()).isEqualTo(dto.getInstrumentDescription());
    assertThat(result.getParticipants()).isEmpty();
    assertThat(result.getTitle()).isEqualTo(dto.getTitle());
    assertThat(result.getBusinessUnit()).isEqualTo(dto.getBusinessUnit());
    assertThat(result.getClassification()).hasSameSizeAs(dto.getClassification());
    assertThat(result.isPromotedToLive()).isEqualTo(dto.isPromotedToLive());
    assertThat(result.getApsHash()).isEqualTo(dto.getApsHash());
    assertThat(result.getAssignee()).isEqualTo(dto.getAssignee());
    assertThat(result.isPromotedToLive()).isEqualTo(dto.isPromotedToLive());
  }

  @Test
  public void shouldReturnEmptyDtoWhenEmptyRequest() {
    SandboxAlertDTO result = mapper.requestToDTO(null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldReturnValidDtoWhenValidRequest() {
    SandboxAlertDTO result = mapper.requestToDTO(request);
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

  }

  @Test
  public void shouldReturnEmptyOutputWhenEmptyInput() {
    List<SandboxAlert> result = mapper.dtoListToApi(Collections.EMPTY_LIST);

    assertThat(result).hasSize(0);
  }

  @Test
  public void shouldReturnValidOutputWhenValidInput() {

    List<SandboxAlertDTO> dtoList = SandboxAlertTestObjects.getAlertDTOList();
    List<SandboxAlert> result = mapper.dtoListToApi(dtoList);

    assertThat(result).hasSize(dtoList.size());
    assertThat(result.get(0).getAlertId())
        .isEqualTo(dtoList.get(0).getPrimaryKey().getAlertId().toString());
    assertThat(result.get(0).getRunId())
        .isEqualTo(dtoList.get(0).getPrimaryKey().getRunId().toString());
  }

  @Test
  public void shouldReturnValidOutputWhenValidCommentRequest() {
    SandboxAlertCommentCreateRequest commentCreateRequest = SandboxAlertTestObjects
        .getCommentCreateRequest();
    SandboxAlertCommentDTO result = mapper.commentRequestToDto(commentCreateRequest, "User1");

    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("User1");
    assertThat(result.getComment()).isEqualTo(commentCreateRequest.getComment());
    assertThat(result.getCreationTime()).isNotNull();
    assertThat(result.getAlertId()).isNull();
    assertThat(result.getCommentId()).isNotNull();
  }

  @Test
  public void whenInvalidCommentRequestThenInvalidOutput() {
    SandboxAlertCommentDTO result = mapper.commentRequestToDto(null, null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldReturnValidOutputWhenValidCommentDto() {
    SandboxAlertCommentDTO commentDto = SandboxAlertTestObjects
        .getCommentDTO(TestUtils.randomUUID(), TestUtils.randomUUID(), "User1", "Comment1",
            TestUtils.randomInstant());
    SandboxAlertComment result = mapper.commentDtoToApi(commentDto);

    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo(commentDto.getUsername());
    assertThat(result.getComment()).isEqualTo(commentDto.getComment());
    assertThat(result.getCreationTime()).isEqualTo(commentDto.getCreationTime());
    assertThat(result.getAlertId()).isEqualTo(commentDto.getAlertId().toString());
    assertThat(result.getCommentId()).isEqualTo(commentDto.getCommentId().toString());
  }

  @Test
  public void shouldReturnInvalidOutputWhenInvalidCommentDto() {
    SandboxAlertComment comment = mapper.commentDtoToApi(null);
    assertThat(comment).isNull();
  }

  @Test
  public void shouldReturnValidOutputWhenValidCommentList() {
    List<SandboxAlertCommentDTO> dtoList = SandboxAlertTestObjects.getCommentDTOList();
    List<SandboxAlertComment> result = mapper.mapCommentsList(dtoList);

    assertThat(result).isNotNull();
    assertThat(result).hasSize(dtoList.size());
  }

  @Test
  public void shouldReturnInvalidOutputWhenInvalidCommentList() {
    List<SandboxAlertComment> result = mapper.mapCommentsList(null);
    assertThat(result).isEmpty();
  }

  @Test
  public void shouldReturnEmptyEntityRelationshipApiWhenEmptyDTO() {
    SandboxAlertEntityRelationship result = mapper.relationshipToApi(null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldReturnValidEntityRelationshipApiWhenValidDTO() {
    SandboxAlertEntityRelationship result = mapper.relationshipToApi(relationship);

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
    List<SandboxAlertEntityRelationshipAudit> auditList = mapper
        .mapRelationshipAuditListToApi(null);
    assertThat(auditList).isNotNull();
    assertThat(auditList).isEmpty();

    auditList = mapper.mapRelationshipAuditListToApi(Collections.emptyList());
    assertThat(auditList).isNotNull();
    assertThat(auditList).isEmpty();

    List<RelationshipAudit> relationshipAudits = SandboxAlertTestObjects
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
    List<RelationshipAudit> relationshipAudits = SandboxAlertTestObjects
        .getRelationshipAudits(alertId, entityId, EntityType.SANDBOXALERT,
            EntityType.TAG);
    List<SandboxAlertEntityRelationshipAudit> auditList = mapper
        .mapRelationshipAuditListToApi(relationshipAudits);
    assertThat(auditList).isNotNull();
    assertThat(auditList).hasSize(relationshipAudits.size());
    assertThat(auditList.get(0).getFromId()).isEqualTo(alertId.toString());
    assertThat(auditList.get(0).getToId()).isEqualTo(entityId.toString());
    assertThat(auditList.get(0).getOperation())
        .isEqualTo(SandboxAlertEntityRelationshipAudit.OperationEnum.ADD);
    assertThat(auditList.get(1).getFromId()).isEqualTo(alertId.toString());
    assertThat(auditList.get(1).getToId()).isEqualTo(entityId.toString());
    assertThat(auditList.get(1).getOperation())
        .isEqualTo(SandboxAlertEntityRelationshipAudit.OperationEnum.REMOVE);
  }

  @Test
  public void shouldReturnEmptyAuditDTOForInvalidAlertDTO() {
    SandboxAlertAuditDTO auditDTO = mapper.dtoToAuditDTO(null);
    assertThat(auditDTO).isNull();
  }


  @Test
  public void shouldReturnValidAuditDTOForValidAlertDTO() {
    SandboxAlertAuditDTO auditDTO = mapper.dtoToAuditDTO(dto);
    assertThat(auditDTO).isNotNull();
    assertThat(auditDTO.getPrimaryKey()).isNotNull();
    assertThat(auditDTO.getPrimaryKey().getAlertId()).isEqualTo(dto.getPrimaryKey().getAlertId());
    assertThat(auditDTO.getPrimaryKey().getAuditId()).isNotNull();
    assertThat(auditDTO.getState()).isEqualTo(dto.getState());
    assertThat(auditDTO.isPromotedToLive()).isEqualTo(dto.isPromotedToLive());
  }

  @Test
  public void shouldReturnEmptyApiListForEmptyOrNullAuditDtoList() {
    List<SandboxAlertAudit> alertAudits = mapper.auditDTOListToApi(null);
    assertThat(alertAudits).isNotNull();
    assertThat(alertAudits).isEmpty();

    alertAudits = mapper.auditDTOListToApi(Collections.emptyList());
    assertThat(alertAudits).isNotNull();
    assertThat(alertAudits).isEmpty();

    List<SandboxAlertAuditDTO> audits = SandboxAlertTestObjects.getAlertAuditsWithNulls();
    alertAudits = mapper.auditDTOListToApi(audits);
    assertThat(alertAudits).isNotNull();
    assertThat(alertAudits).isEmpty();
  }


  @Test
  public void shouldReturnValidApiListForValidAlertAuditDtoList() {
    List<SandboxAlertAuditDTO> auditDTOS = SandboxAlertTestObjects.getAlertAuditDTOs();
    List<SandboxAlertAudit> alertAudits = mapper.auditDTOListToApi(auditDTOS);
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
    assertThat(alertAudits.get(0).getRunId())
        .isEqualTo(auditDTOS.get(0).getRunId().toString());
    assertThat(alertAudits.get(0).getUpdatedBy())
        .isEqualTo(auditDTOS.get(0).getUpdatedBy());
    assertThat(alertAudits.get(0).isPromotedToLive())
        .isEqualTo(auditDTOS.get(0).isPromotedToLive());

  }
}
