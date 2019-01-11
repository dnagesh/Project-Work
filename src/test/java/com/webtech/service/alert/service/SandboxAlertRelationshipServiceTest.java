package com.webtech.service.alert.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alert.SandboxAlertTestObjects;
import com.webtech.service.alert.dto.SandboxAlertDTO;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SandboxAlertRelationshipServiceTest {

  UUID alertId;
  UUID runId;
  private SandboxAlertRelationshipService alertRelationshipService;
  @Mock
  private SandboxAlertDTORepository mockRepository;
  @Mock
  private EntityRelationshipService mockEntityRelationshipService;
  private SandboxAlertObjectMapper mapper;

  @Before
  public void setUp() throws Exception {
    mapper = new SandboxAlertObjectMapper();
    alertRelationshipService = new SandboxAlertRelationshipService(mockEntityRelationshipService,
        mockRepository, mapper);

    alertId = TestUtils.randomUUID();
    runId = TestUtils.randomUUID();
    SandboxAlertDTO alertDTO = SandboxAlertTestObjects
        .getAlertDTO(alertId, runId, "alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
            "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
            "Dave Jones");

    when(mockRepository.findById(any())).thenReturn(Optional.ofNullable(alertDTO));
  }


  @Test
  public void shouldLinkTagWhenValidAlert() {

    //Tag to link
    UUID tagId = TestUtils.randomUUID();
    //Username
    String username = TestUtils.randomAlphanumeric(10);

    Relationship savedRelationship = SandboxAlertTestObjects
        .getRelationship(alertId, tagId, EntityType.SANDBOXALERT, EntityType.TAG, username);
    savedRelationship.setWhen(TestUtils.randomInstant());

    when(mockEntityRelationshipService
        .createEntityRelationship(alertId, tagId, EntityType.SANDBOXALERT, EntityType.TAG,
            username))
        .thenReturn(savedRelationship);

    SandboxAlertEntityLinkRequest linkRequest = SandboxAlertTestObjects.getEntityLinkRequest(tagId);
    try {
      SandboxAlertEntityRelationship result = alertRelationshipService
          .createRelationship(alertId.toString(), runId.toString(), linkRequest,
              EntityType.SANDBOXALERT, EntityType.TAG,
              username);
      verify(mockEntityRelationshipService, times(1)).createEntityRelationship(alertId, tagId,
          EntityType.SANDBOXALERT, EntityType.TAG, username);

      assertThat(result).isNotNull();
      assertThat(result.getFromId()).isEqualTo(alertId.toString());
      assertThat(result.getToId()).isEqualTo(tagId.toString());
      assertThat(result.getFromType()).isEqualTo(EntityType.SANDBOXALERT.toString());
      assertThat(result.getToType()).isEqualTo(EntityType.TAG.toString());
      assertThat(result.getWhen()).isNotNull();
    } catch (EntityNotFoundException e) {
      fail("Not Expected: EntityNotFoundException");
    }

  }

  @Test
  public void shouldThrowAlertNotFoundExceptionInLinkTagWhenInvalidAlert() {
    UUID alertId = TestUtils.randomUUID();

    when(mockRepository.findById(any())).thenReturn(Optional.ofNullable(null));

    //Tag to link
    UUID tagId = TestUtils.randomUUID();
    SandboxAlertEntityLinkRequest linkRequest = SandboxAlertTestObjects.getEntityLinkRequest(tagId);

    try {
      SandboxAlertEntityRelationship result = alertRelationshipService
          .createRelationship(alertId.toString(), runId.toString(), linkRequest,
              EntityType.SANDBOXALERT, EntityType.TAG,
              TestUtils.randomAlphanumeric(10));
      fail("Expected EntityNotFoundException");
    } catch (EntityNotFoundException e) {
      assertThat(e.getMessage()).contains(alertId.toString());
      assertThat(e.getMessage()).contains(runId.toString());
    }

  }


  @Test
  public void shouldDelinkTagWhenValidAlert() {

    //Tag to delink
    UUID tagId = TestUtils.randomUUID();

    //Username
    String username = TestUtils.randomAlphanumeric(10);

    Relationship savedRelationship = SandboxAlertTestObjects
        .getRelationship(alertId, tagId, EntityType.SANDBOXALERT, EntityType.TAG, username);
    savedRelationship.setWhen(TestUtils.randomInstant());

    when(mockEntityRelationshipService
        .removeEntityRelationship(alertId, tagId, EntityType.SANDBOXALERT, EntityType.TAG,
            username))
        .thenReturn(savedRelationship);

    try {
      SandboxAlertEntityRelationship result = alertRelationshipService
          .deleteRelationship(alertId.toString(), runId.toString(), tagId.toString(),
              EntityType.SANDBOXALERT, EntityType.TAG, username);

      verify(mockEntityRelationshipService, times(1)).removeEntityRelationship(alertId, tagId,
          EntityType.SANDBOXALERT, EntityType.TAG, username);

      assertThat(result).isNotNull();
      assertThat(result.getFromId()).isEqualTo(alertId.toString());
      assertThat(result.getToId()).isEqualTo(tagId.toString());
      assertThat(result.getFromType()).isEqualTo(EntityType.SANDBOXALERT.toString());
      assertThat(result.getToType()).isEqualTo(EntityType.TAG.toString());
      assertThat(result.getWhen()).isNotNull();
    } catch (EntityNotFoundException e) {
      fail("Not Expected: EntityNotFoundException");
    }

  }

  @Test
  public void shouldThrowAlertNotFoundExceptionInDelinkTagWhenInvalidAlert() {
    UUID alertId = TestUtils.randomUUID();

    when(mockRepository.findById(any())).thenReturn(Optional.ofNullable(null));

    //Tag to link
    UUID tagId = TestUtils.randomUUID();
    try {
      SandboxAlertEntityRelationship result = alertRelationshipService
          .deleteRelationship(alertId.toString(), runId.toString(), tagId.toString(),
              EntityType.SANDBOXALERT, EntityType.TAG, TestUtils.randomAlphanumeric(10));
      fail("Expected EntityNotFoundException");
    } catch (EntityNotFoundException e) {
      assertThat(e.getMessage()).contains(alertId.toString());
      assertThat(e.getMessage()).contains(runId.toString());
    }

  }

  @Test
  public void shouldReturnResultsWhenLinkedTagsFound() throws Throwable {
    UUID alertId = TestUtils.randomUUID();
    List<Relationship> relationships = SandboxAlertTestObjects.getRelationships(alertId);
    when(mockEntityRelationshipService.getRelationships(any())).thenReturn(relationships);
    List<String> tags = alertRelationshipService
        .getRelationshipsByAlertId(alertId.toString(), runId.toString(), EntityType.TAG);

    assertThat(tags).isNotNull();
    assertThat(tags).hasSize(2);
  }

  @Test
  public void shouldNotReturnResultsWhenNoLinkedTagsFound() throws Throwable {
    UUID alertId = TestUtils.randomUUID();
    List<String> tags = alertRelationshipService
        .getRelationshipsByAlertId(alertId.toString(), runId.toString(), EntityType.TAG);

    assertThat(tags).isNotNull();
    assertThat(tags).hasSize(0);
  }

  @Test(expected = EntityNotFoundException.class)
  public void shouldReturnExceptionWhenAlertNotFound() throws Throwable {
    when(mockRepository.findById(any())).thenReturn(Optional.empty());
    List<String> tags = alertRelationshipService
        .getRelationshipsByAlertId(alertId.toString(), runId.toString(), EntityType.TAG);
  }

  @Test
  public void shouldNotReturnDataWhenNoAuditHistoryFoundForRelationship() throws Throwable {
    UUID tagId = TestUtils.randomUUID();

    List<SandboxAlertEntityRelationshipAudit> result = alertRelationshipService
        .getRelationshipAudit(alertId.toString(), runId.toString(), tagId.toString());
    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  public void shouldReturnDataWhenAuditHistoryFoundForRelationship() throws Throwable {
    UUID tagId = TestUtils.randomUUID();

    List<RelationshipAudit> relationshipAudits = SandboxAlertTestObjects
        .getRelationshipAudits(alertId, tagId, EntityType.SANDBOXALERT,
            EntityType.TAG);
    when(mockEntityRelationshipService.getRelationshipAudit(alertId, tagId))
        .thenReturn(relationshipAudits);

    List<SandboxAlertEntityRelationshipAudit> result = alertRelationshipService
        .getRelationshipAudit(alertId.toString(), runId.toString(), tagId.toString());
    assertThat(result).isNotNull();
    assertThat(result).hasSize(relationshipAudits.size());
    assertThat(result.get(0).getFromId()).isEqualTo(alertId.toString());
    assertThat(result.get(0).getToId()).isEqualTo(tagId.toString());

  }

  @Test
  public void shouldThrowAlertNotFoundExceptionWhenNoAlertFoundForRelationship() {
    UUID tagId = TestUtils.randomUUID();

    when(mockRepository.findById(any())).thenReturn(Optional.ofNullable(null));

    try {
      List<SandboxAlertEntityRelationshipAudit> result = alertRelationshipService
          .getRelationshipAudit(alertId.toString(), runId.toString(), tagId.toString());
      fail("Cannot find audit for invalid Alert");
    } catch (EntityNotFoundException e) {
      assertThat(e.getMessage()).contains(alertId.toString());
      assertThat(e.getMessage()).contains(runId.toString());
    }

  }


}
