package com.webtech.service.alertconfiguration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alertconfiguration.TestObjects;
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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlertConfigurationRelationshipServiceTest {


  @Mock
  private LiveAlertConfigurationRepository mockLiveAlertRepository;
  @Mock
  private SandboxAlertConfigurationRepository mockSandboxAlertRepository;
  @Mock
  private EntityRelationshipService mockEntityRelationshipService;

  private AlertConfigurationRelationshipService alertConfigurationRelationshipService;

  @Before
  public void setup() {
    alertConfigurationRelationshipService = new AlertConfigurationRelationshipService(
        mockEntityRelationshipService,
        mockLiveAlertRepository, mockSandboxAlertRepository, new RelationshipMapper(),
        new EntityRelationshipAuditMapper());
  }


  @Test
  public void shouldReturnResultsWhenLinkedAlertConfigsFound() {
    UUID alertConfigurationid = TestUtils.randomUUID();
    List<Relationship> relationships = TestObjects.getRelationships(alertConfigurationid);
    when(mockEntityRelationshipService.getRelationships(any())).thenReturn(relationships);
    List<String> alertConfigurations = alertConfigurationRelationshipService
        .getRelationshipsByAlertConfigurationId(alertConfigurationid.toString(), EntityType.TAG);
    assertThat(alertConfigurations).isNotNull();
    assertThat(alertConfigurations).hasSize(2);
  }

  @Test
  public void shouldNotReturnResultsWhenNoLinkedAlertConfigsFound() {
    UUID alertConfigurationid = TestUtils.randomUUID();
    List<String> alerts = alertConfigurationRelationshipService
        .getRelationshipsByAlertConfigurationId(alertConfigurationid.toString(),
            EntityType.LIVEALERTCONFIGURATION);
    assertThat(alerts).isNotNull();
    assertThat(alerts).hasSize(0);
  }


  @Test
  public void shouldThrowAlertConfigurationNotFoundExceptionIfAlertConfigNotFoundForCreateRelationship() {
    UUID alertConfigurationUUID = TestUtils.randomUUID();

    when(mockLiveAlertRepository.findById(alertConfigurationUUID))
        .thenReturn(Optional.ofNullable(null));

    //Tag to link
    UUID tagId = TestUtils.randomUUID();
    EntityLinkRequest linkRequest = TestObjects.getEntityLinkRequest(tagId);

    try {
      alertConfigurationRelationshipService
          .createRelationship(alertConfigurationUUID.toString(), linkRequest,
              EntityType.LIVEALERTCONFIGURATION, EntityType.TAG,
              TestUtils.randomAlphanumeric(10), null);
      fail("Expected AlertConfigurationNotFoundException");
    } catch (AlertConfigurationNotFoundException e) {
      assertThat(e.getMessage())
          .isEqualTo("Failed to find live alert configuration with ID " + alertConfigurationUUID);
    }

  }


  @Test
  public void shouldReturnEntityRelationshipWhenAlertConfigFoundForCreateRelationship() {
    UUID alertConfigurationId = TestUtils.randomUUID();

    LiveAlertConfigurationDTO dto = new LiveAlertConfigurationDTO();
    dto.setUuid(alertConfigurationId);

    when(mockLiveAlertRepository.findById(alertConfigurationId))
        .thenReturn(Optional.ofNullable(dto));

    //Tag to link
    UUID tagId = TestUtils.randomUUID();
    //Username
    String username = TestUtils.randomAlphanumeric(10);
    Relationship savedRelationship = TestObjects
        .getRelationship(alertConfigurationId, tagId, EntityType.LIVEALERTCONFIGURATION,
            EntityType.TAG,
            username);
    savedRelationship.setWhen(TestUtils.randomInstant());
    when(mockEntityRelationshipService
        .createEntityRelationship(alertConfigurationId, tagId, EntityType.LIVEALERTCONFIGURATION,
            EntityType.TAG, username))
        .thenReturn(savedRelationship);

    EntityLinkRequest linkRequest = TestObjects.getEntityLinkRequest(tagId);
    try {
      EntityRelationship result = alertConfigurationRelationshipService
          .createRelationship(alertConfigurationId.toString(), linkRequest,
              EntityType.LIVEALERTCONFIGURATION, EntityType.TAG,
              username, null);
      verify(mockEntityRelationshipService, times(1))
          .createEntityRelationship(alertConfigurationId, tagId,
              EntityType.LIVEALERTCONFIGURATION, EntityType.TAG, username);

      assertThat(result).isNotNull();
      assertThat(result.getFromId()).isEqualTo(alertConfigurationId.toString());
      assertThat(result.getToId()).isEqualTo(tagId.toString());
      assertThat(result.getFromType()).isEqualTo(EntityType.LIVEALERTCONFIGURATION.toString());
      assertThat(result.getToType()).isEqualTo(EntityType.TAG.toString());
      assertThat(result.getWhen()).isNotNull();
    } catch (AlertConfigurationNotFoundException e) {
      fail("Not Expected: AlertNotFoundException");
    }

  }

  @Test
  public void shouldDeleteTheLiveAlertConfigTagRelationshipWhenValidTagIdForDeleteRelationship() {
    UUID alertConfigurationId = TestUtils.randomUUID();
    //Tag to delink
    UUID tagId = TestUtils.randomUUID();

    //Username
    String username = TestUtils.randomAlphanumeric(10);

    LiveAlertConfigurationDTO dto = new LiveAlertConfigurationDTO();
    dto.setUuid(alertConfigurationId);
    when(mockLiveAlertRepository.findById(alertConfigurationId))
        .thenReturn(Optional.ofNullable(dto));
    Relationship savedRelationship = TestObjects
        .getRelationship(alertConfigurationId, tagId, EntityType.LIVEALERTCONFIGURATION,
            EntityType.TAG,
            username);
    savedRelationship.setWhen(TestUtils.randomInstant());
    when(mockEntityRelationshipService
        .removeEntityRelationship(alertConfigurationId, tagId, EntityType.LIVEALERTCONFIGURATION,
            EntityType.TAG, username))
        .thenReturn(savedRelationship);

    try {
      EntityRelationship result = alertConfigurationRelationshipService
          .deleteRelationship(alertConfigurationId.toString(), tagId.toString(),
              EntityType.LIVEALERTCONFIGURATION, EntityType.TAG, username, null);

      verify(mockEntityRelationshipService, times(1))
          .removeEntityRelationship(alertConfigurationId, tagId,
              EntityType.LIVEALERTCONFIGURATION, EntityType.TAG, username);

      assertThat(result).isNotNull();
      assertThat(result.getFromId()).isEqualTo(alertConfigurationId.toString());
      assertThat(result.getToId()).isEqualTo(tagId.toString());
      assertThat(result.getFromType()).isEqualTo(EntityType.LIVEALERTCONFIGURATION.toString());
      assertThat(result.getToType()).isEqualTo(EntityType.TAG.toString());
      assertThat(result.getWhen()).isNotNull();
    } catch (AlertConfigurationNotFoundException e) {
      fail("Not Expected: AlertConfigurationNotFoundException");
    }

  }

  @Test
  public void shouldThrowAlertConfigurationNotFoundExceptionWhenInvalidAlertConfigForDeleteRelationship() {
    UUID alertConfigurationId = TestUtils.randomUUID();

    when(mockLiveAlertRepository.findById(alertConfigurationId))
        .thenReturn(Optional.ofNullable(null));

    //Tag to link
    UUID tagId = TestUtils.randomUUID();
    try {
      alertConfigurationRelationshipService
          .deleteRelationship(alertConfigurationId.toString(), tagId.toString(),
              EntityType.LIVEALERTCONFIGURATION, EntityType.TAG, TestUtils.randomAlphanumeric(10),
              null);
      fail("Expected AlertConfigurationNotFoundException");
    } catch (AlertConfigurationNotFoundException e) {
      assertThat(e.getMessage())
          .isEqualTo("Failed to find live alert configuration with ID " + alertConfigurationId);
    }

  }

  @Test
  public void shouldReturnResultsWhenLinkedTagsFoundForGetRelationshipsByAlertConfigurationId() {
    UUID alertConfigurationId = TestUtils.randomUUID();
    List<Relationship> relationships = TestObjects.getRelationships(alertConfigurationId);
    when(mockEntityRelationshipService.getRelationships(any())).thenReturn(relationships);
    List<String> tags = alertConfigurationRelationshipService
        .getRelationshipsByAlertConfigurationId(alertConfigurationId.toString(), EntityType.TAG);
    assertThat(tags).isNotNull();
  }

  @Test
  public void shouldNotReturnResultsWhenNoLinkedTagsFoundForGetRelationshipsByAlertConfigurationId() {
    UUID alertConfigurationId = TestUtils.randomUUID();
    List<String> tags = alertConfigurationRelationshipService
        .getRelationshipsByAlertConfigurationId(alertConfigurationId.toString(), EntityType.TAG);
    assertThat(tags).isNotNull();
    assertThat(tags).hasSize(0);
  }


  @Test
  public void shouldNotReturnDataWhenNoAuditHistoryFoundForGetRelationshipAudit() throws Throwable {
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    LiveAlertConfigurationDTO dto = TestObjects
        .getLiveConfigurationDTO(alertConfigurationId, "Market Abuse", "Market Abuse", "ACTIVE",
            Instant.now(), "Unit Tester", Instant.now(), "Integration Tester", "");

    when(mockLiveAlertRepository.findById(alertConfigurationId))
        .thenReturn(Optional.ofNullable(dto));
    List<EntityRelationshipAudit> result = alertConfigurationRelationshipService
        .getRelationshipAudit(alertConfigurationId.toString(), tagId.toString(), null);
    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  public void shouldReturnDataWhenAuditHistoryFoundForGetRelationshipAudit() throws Throwable {
    LiveAlertConfigurationDTO dto = TestObjects
        .getLiveConfigurationDTO(TestUtils.randomUUID(), "Market Abuse", "Market Abuse", "ACTIVE",
            Instant.now(), "Unit Tester", Instant.now(), "Integration Tester", "");
    UUID alertConfigurationId = dto.getUuid();
    UUID tagId = TestUtils.randomUUID();
    when(mockLiveAlertRepository.findById(alertConfigurationId))
        .thenReturn(Optional.ofNullable(dto));

    List<RelationshipAudit> relationshipAudits = TestObjects
        .getRelationshipAudits(alertConfigurationId, tagId, EntityType.LIVEALERTCONFIGURATION,
            EntityType.TAG);
    when(mockEntityRelationshipService.getRelationshipAudit(alertConfigurationId, tagId))
        .thenReturn(relationshipAudits);
    List<EntityRelationshipAudit> result = alertConfigurationRelationshipService
        .getRelationshipAudit(alertConfigurationId.toString(), tagId.toString(), null);

    assertThat(result).isNotNull();
    assertThat(result).hasSize(relationshipAudits.size());
    assertThat(result.get(0).getFromId()).isEqualTo(alertConfigurationId.toString());
    assertThat(result.get(0).getToId()).isEqualTo(tagId.toString());

  }

  @Test(expected = AlertConfigurationNotFoundException.class)
  public void shouldThrowAlertConfigurationNotFoundExceptionIfAlertConfigIsInvalidForGetRelationshipAudit()
      throws Throwable {
    UUID alertConfigurationid = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    when(mockLiveAlertRepository.findById(alertConfigurationid))
        .thenReturn(Optional.ofNullable(null));
    alertConfigurationRelationshipService
        .getRelationshipAudit(alertConfigurationid.toString(), tagId.toString(), null);
  }

  //SandboxAlertConfiguration
  @Test
  public void shouldThrowAlertConfigurationNotFoundExceptionIfSandboxAlertConfigNotFoundForCreateRelationship() {

    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationUUID = TestUtils.randomUUID();
    SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey();
    primaryKey.setAlertConfigurationUUID(alertConfigurationUUID);
    primaryKey.setSandboxUUID(sandboxId);

    when(mockSandboxAlertRepository.findById(primaryKey)).thenReturn(Optional.ofNullable(null));

    //Tag to link
    UUID tagId = TestUtils.randomUUID();
    EntityLinkRequest linkRequest = TestObjects.getEntityLinkRequest(tagId);

    try {
      alertConfigurationRelationshipService
          .createRelationship(alertConfigurationUUID.toString(), linkRequest,
              EntityType.SANDBOXALERTCONFIGURATION, EntityType.TAG,
              TestUtils.randomAlphanumeric(10), sandboxId.toString());
      fail("Expected AlertConfigurationNotFoundException");
    } catch (AlertConfigurationNotFoundException e) {
      assertThat(e.getMessage())
          .isEqualTo("Failed to find live alert configuration with ID " + alertConfigurationUUID);
    }

  }


  @Test
  public void shouldReturnEntityRelationshipWhenSandboxAlertConfigFoundForCreateRelationship() {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey();
    primaryKey.setAlertConfigurationUUID(alertConfigurationId);
    primaryKey.setSandboxUUID(sandboxId);

    SandboxAlertConfigurationDTO dto = new SandboxAlertConfigurationDTO();
    dto.setPrimaryKey(primaryKey);

    when(mockSandboxAlertRepository.findById(primaryKey)).thenReturn(Optional.ofNullable(dto));

    //Tag to link
    UUID tagId = TestUtils.randomUUID();
    //Username
    String username = TestUtils.randomAlphanumeric(10);
    Relationship savedRelationship = TestObjects
        .getRelationship(alertConfigurationId, tagId, EntityType.SANDBOXALERTCONFIGURATION,
            EntityType.TAG,
            username);
    savedRelationship.setWhen(TestUtils.randomInstant());
    when(mockEntityRelationshipService
        .createEntityRelationship(alertConfigurationId, tagId, EntityType.SANDBOXALERTCONFIGURATION,
            EntityType.TAG, username))
        .thenReturn(savedRelationship);

    EntityLinkRequest linkRequest = TestObjects.getEntityLinkRequest(tagId);
    try {
      EntityRelationship result = alertConfigurationRelationshipService
          .createRelationship(alertConfigurationId.toString(), linkRequest,
              EntityType.SANDBOXALERTCONFIGURATION, EntityType.TAG,
              username, sandboxId.toString());
      verify(mockEntityRelationshipService, times(1))
          .createEntityRelationship(alertConfigurationId, tagId,
              EntityType.SANDBOXALERTCONFIGURATION, EntityType.TAG, username);

      assertThat(result).isNotNull();
      assertThat(result.getFromId()).isEqualTo(alertConfigurationId.toString());
      assertThat(result.getToId()).isEqualTo(tagId.toString());
      assertThat(result.getFromType()).isEqualTo(EntityType.SANDBOXALERTCONFIGURATION.toString());
      assertThat(result.getToType()).isEqualTo(EntityType.TAG.toString());
      assertThat(result.getWhen()).isNotNull();
    } catch (AlertConfigurationNotFoundException e) {
      fail("Not Expected: AlertNotFoundException");
    }

  }


  @Test
  public void shouldDeleteTheSandboxAlertConfigTagRelationshipWhenValidTagIdForDeleteRelationship() {

    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    //Tag to delink
    UUID tagId = TestUtils.randomUUID();

    //Username
    String username = TestUtils.randomAlphanumeric(10);

    SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey();
    primaryKey.setAlertConfigurationUUID(alertConfigurationId);
    primaryKey.setSandboxUUID(sandboxId);
    SandboxAlertConfigurationDTO dto = new SandboxAlertConfigurationDTO();
    dto.setPrimaryKey(primaryKey);
    when(mockSandboxAlertRepository.findById(primaryKey)).thenReturn(Optional.ofNullable(dto));
    Relationship savedRelationship = TestObjects
        .getRelationship(alertConfigurationId, tagId, EntityType.SANDBOXALERTCONFIGURATION,
            EntityType.TAG,
            username);
    savedRelationship.setWhen(TestUtils.randomInstant());
    when(mockEntityRelationshipService
        .removeEntityRelationship(alertConfigurationId, tagId, EntityType.SANDBOXALERTCONFIGURATION,
            EntityType.TAG, username))
        .thenReturn(savedRelationship);

    try {
      EntityRelationship result = alertConfigurationRelationshipService
          .deleteRelationship(alertConfigurationId.toString(), tagId.toString(),
              EntityType.SANDBOXALERTCONFIGURATION, EntityType.TAG, username, sandboxId.toString()
          );

      verify(mockEntityRelationshipService, times(1))
          .removeEntityRelationship(alertConfigurationId, tagId,
              EntityType.SANDBOXALERTCONFIGURATION, EntityType.TAG, username);

      assertThat(result).isNotNull();
      assertThat(result.getFromId()).isEqualTo(alertConfigurationId.toString());
      assertThat(result.getToId()).isEqualTo(tagId.toString());
      assertThat(result.getFromType()).isEqualTo(EntityType.SANDBOXALERTCONFIGURATION.toString());
      assertThat(result.getToType()).isEqualTo(EntityType.TAG.toString());
      assertThat(result.getWhen()).isNotNull();
    } catch (AlertConfigurationNotFoundException e) {
      fail("Not Expected: AlertConfigurationNotFoundException");
    }

  }

  @Test
  public void shouldThrowAlertConfigurationNotFoundExceptionWhenInvalidSandboxAlertConfigForDeleteRelationship() {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey();
    primaryKey.setAlertConfigurationUUID(alertConfigurationId);
    primaryKey.setSandboxUUID(sandboxId);

    when(mockSandboxAlertRepository.findById(primaryKey)).thenReturn(Optional.ofNullable(null));

    //Tag to link
    UUID tagId = TestUtils.randomUUID();
    try {
      alertConfigurationRelationshipService
          .deleteRelationship(alertConfigurationId.toString(), tagId.toString(),
              EntityType.SANDBOXALERTCONFIGURATION, EntityType.TAG,
              TestUtils.randomAlphanumeric(10), sandboxId.toString());
      fail("Expected AlertConfigurationNotFoundException");
    } catch (AlertConfigurationNotFoundException e) {
      assertThat(e.getMessage())
          .isEqualTo("Failed to find live alert configuration with ID " + alertConfigurationId);
    }

  }


}
