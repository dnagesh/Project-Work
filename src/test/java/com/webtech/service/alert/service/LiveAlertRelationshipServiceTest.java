package com.webtech.service.alert.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alert.LiveAlertTestObjects;
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
public class LiveAlertRelationshipServiceTest {

  @Mock
  private LiveAlertRepository mockRepository;

  @Mock
  private EntityRelationshipService mockEntityRelationshipService;

  private LiveAlertRelationshipService alertRelationshipService;

  private UUID alertId;
  private LiveAlertDTO dto;
  private UUID tagId;
  private String username;
  private EntityLinkRequest linkRequest;

  @Before
  public void setup() {
    alertRelationshipService = new LiveAlertRelationshipService(mockEntityRelationshipService,
        mockRepository, new LiveAlertObjectMapper());

    alertId = TestUtils.randomUUID();

    dto = LiveAlertTestObjects
        .getAlertDTO(alertId, "alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
            "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
            "Dave Jones");

    when(mockRepository.findById(alertId)).thenReturn(Optional.ofNullable(dto));

    //Tag to link
    tagId = TestUtils.randomUUID();
    //Username
    username = TestUtils.randomAlphanumeric(10);

    Relationship savedRelationship = LiveAlertTestObjects
        .getRelationship(alertId, tagId, EntityType.ALERT, EntityType.TAG, username);
    savedRelationship.setWhen(TestUtils.randomInstant());

    when(mockEntityRelationshipService
        .createEntityRelationship(alertId, tagId, EntityType.ALERT, EntityType.TAG, username))
        .thenReturn(savedRelationship);

    when(mockEntityRelationshipService
        .removeEntityRelationship(alertId, tagId, EntityType.ALERT, EntityType.TAG, username))
        .thenReturn(savedRelationship);

    linkRequest = LiveAlertTestObjects.getEntityLinkRequest(tagId);
  }

  @Test
  public void shouldLinkTagWhenValidAlert() {
    try {
      EntityRelationship result = alertRelationshipService
          .createRelationship(alertId.toString(), linkRequest, EntityType.ALERT, EntityType.TAG,
              username);
      verify(mockEntityRelationshipService, times(1)).createEntityRelationship(alertId, tagId,
          EntityType.ALERT, EntityType.TAG, username);

      assertThat(result).isNotNull();
      assertThat(result.getFromId()).isEqualTo(alertId.toString());
      assertThat(result.getToId()).isEqualTo(tagId.toString());
      assertThat(result.getFromType()).isEqualTo(EntityType.ALERT.toString());
      assertThat(result.getToType()).isEqualTo(EntityType.TAG.toString());
      assertThat(result.getWhen()).isNotNull();
    } catch (EntityNotFoundException e) {
      fail("Not Expected: EntityNotFoundException");
    }

  }

  @Test
  public void shouldThrowAlertNotFoundExceptionInLinkTagWhenInvalidAlert() {
    when(mockRepository.findById(alertId)).thenReturn(Optional.ofNullable(null));
    try {
      EntityRelationship result = alertRelationshipService
          .createRelationship(alertId.toString(), linkRequest, EntityType.ALERT, EntityType.TAG,
              TestUtils.randomAlphanumeric(10));
      fail("Expected EntityNotFoundException");
    } catch (EntityNotFoundException e) {
      assertThat(e.getMessage()).contains(alertId.toString());
    }

  }


  @Test
  public void shouldDelinkTagWhenValidAlert() {
    try {
      EntityRelationship result = alertRelationshipService
          .deleteRelationship(alertId.toString(), tagId.toString(),
              EntityType.ALERT, EntityType.TAG, username);

      verify(mockEntityRelationshipService, times(1)).removeEntityRelationship(alertId, tagId,
          EntityType.ALERT, EntityType.TAG, username);

      assertThat(result).isNotNull();
      assertThat(result.getFromId()).isEqualTo(alertId.toString());
      assertThat(result.getToId()).isEqualTo(tagId.toString());
      assertThat(result.getFromType()).isEqualTo(EntityType.ALERT.toString());
      assertThat(result.getToType()).isEqualTo(EntityType.TAG.toString());
      assertThat(result.getWhen()).isNotNull();
    } catch (EntityNotFoundException e) {
      fail("Not Expected: EntityNotFoundException");
    }

  }

  @Test
  public void shouldThrowAlertNotFoundExceptionInDelinkTagWhenInvalidAlert() {
    when(mockRepository.findById(alertId)).thenReturn(Optional.ofNullable(null));

    try {
      EntityRelationship result = alertRelationshipService
          .deleteRelationship(alertId.toString(), tagId.toString(),
              EntityType.ALERT, EntityType.TAG, TestUtils.randomAlphanumeric(10));
      fail("Expected EntityNotFoundException");
    } catch (EntityNotFoundException e) {
      assertThat(e.getMessage()).contains(alertId.toString());
    }

  }

  @Test
  public void shouldReturnResultsWhenLinkedTagsFound() {
    UUID alertId = TestUtils.randomUUID();
    List<Relationship> relationships = LiveAlertTestObjects.getRelationships(alertId);
    when(mockEntityRelationshipService.getRelationships(any())).thenReturn(relationships);
    List<String> tags = alertRelationshipService
        .getRelationshipsByAlertId(alertId.toString(), EntityType.TAG);

    assertThat(tags).isNotNull();
    assertThat(tags).hasSize(2);
  }

  @Test
  public void shouldNotReturnResultsWhenNoLinkedTagsFound() {
    UUID alertId = TestUtils.randomUUID();
    List<String> tags = alertRelationshipService
        .getRelationshipsByAlertId(alertId.toString(), EntityType.TAG);

    assertThat(tags).isNotNull();
    assertThat(tags).hasSize(0);
  }

  @Test
  public void shouldNotReturnDataWhenNoAuditHistoryFoundForRelationship() throws Throwable {
    when(mockEntityRelationshipService.getRelationshipAudit(alertId, tagId)).thenReturn(null);

    List<EntityRelationshipAudit> result = alertRelationshipService
        .getRelationshipAudit(alertId.toString(), tagId.toString());
    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  public void shouldReturnDataWhenAuditHistoryFoundForRelationship() throws Throwable {
    List<RelationshipAudit> relationshipAudits = LiveAlertTestObjects
        .getRelationshipAudits(alertId, tagId, EntityType.ALERT,
            EntityType.TAG);
    when(mockEntityRelationshipService.getRelationshipAudit(alertId, tagId))
        .thenReturn(relationshipAudits);

    List<EntityRelationshipAudit> result = alertRelationshipService
        .getRelationshipAudit(alertId.toString(), tagId.toString());
    assertThat(result).isNotNull();
    assertThat(result).hasSize(relationshipAudits.size());
    assertThat(result.get(0).getFromId()).isEqualTo(alertId.toString());
    assertThat(result.get(0).getToId()).isEqualTo(tagId.toString());

  }

  @Test
  public void shouldThrowAlertNotFoundExceptionWhenNoAlertFoundForRelationship() {
    when(mockRepository.findById(alertId)).thenReturn(Optional.ofNullable(null));

    try {
      List<EntityRelationshipAudit> result = alertRelationshipService
          .getRelationshipAudit(alertId.toString(), tagId.toString());
      fail("Cannot find audit for invalid Alert");
    } catch (EntityNotFoundException e) {
      assertThat(e.getMessage()).contains(alertId.toString());
    }

  }

}
