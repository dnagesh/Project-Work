package com.webtech.service.alert.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alert.LiveAlertTestObjects;
import com.webtech.service.alert.dto.LiveAlertAuditDTO;
import com.webtech.service.alert.dto.LiveAlertDTO;
import com.webtech.service.alert.exception.UpdateStateReasonMissingException;
import com.webtech.service.alert.mapper.LiveAlertObjectMapper;
import com.webtech.service.alert.repository.LiveAlertAuditRepository;
import com.webtech.service.alert.repository.LiveAlertRepository;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.irisium.service.livealert.model.Alert;
import com.irisium.service.livealert.model.AlertAudit;
import com.irisium.service.livealert.model.CreateAlertRequest;
import com.irisium.service.livealert.model.UpdateAssigneeRequest;
import com.irisium.service.livealert.model.UpdateStateRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LiveAlertServiceTest {

  private static final String USER = "User";

  @Mock
  private LiveAlertRepository mockRepository;

  @Mock
  private LiveAlertAuditRepository mockAuditRepository;

  private LiveAlertObjectMapper objectMapper;

  @Captor
  private ArgumentCaptor<LiveAlertDTO> argumentDTO;

  @Captor
  private ArgumentCaptor<LiveAlertAuditDTO> argumentAuditDTO;

  private LiveAlertService service;

  private UUID alertId;
  private LiveAlertDTO dto;

  @Before
  public void setup() {
    objectMapper = new LiveAlertObjectMapper();
    service = new LiveAlertService(mockRepository, mockAuditRepository, objectMapper);
    alertId = TestUtils.randomUUID();
    dto = LiveAlertTestObjects
        .getAlertDTO(alertId, "alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
            "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
            "Dave Jones");
    when(mockRepository.findById(alertId)).thenReturn(Optional.ofNullable(dto));
    when(mockRepository.save(any(LiveAlertDTO.class))).thenReturn(dto);
  }


  @Test
  public void shouldCreateAlertWithValidData() {
    CreateAlertRequest createAlertRequest = LiveAlertTestObjects
        .getAlertCreateRequest("alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", Arrays.asList("Eleis Commodities"), "Wash Trade",
            "Europe/Equity", Arrays.asList("Regulatory", "Operational"));
    LiveAlertDTO dto = objectMapper.requestToDto(createAlertRequest);
    dto.setAlertId(TestUtils.randomUUID());
    when(mockRepository.save(ArgumentMatchers.any(LiveAlertDTO.class))).thenReturn(dto);
    Alert alert = service.createAlert(createAlertRequest);

    InOrder inOrder = inOrder(mockAuditRepository, mockRepository);
    inOrder.verify(mockAuditRepository, times(1)).save(argumentAuditDTO.capture());
    inOrder.verify(mockRepository, times(1)).save(argumentDTO.capture());

    assertThat(argumentAuditDTO.getValue()).isNotNull();
    assertThat(argumentAuditDTO.getValue().getPrimaryKey()).isNotNull();
    assertThat(argumentAuditDTO.getValue().getUpdatedBy())
        .isNullOrEmpty();
    assertThat(argumentAuditDTO.getValue().getUpdatedDate())
        .isNull();
    assertThat(argumentAuditDTO.getValue().getCreatedDate())
        .isNotNull();
    assertThat(argumentAuditDTO.getValue().getState()).isEqualTo(dto.getState());

    assertThat(argumentAuditDTO.getValue().getPrimaryKey().getAlertId())
        .isEqualTo(argumentDTO.getValue().getAlertId());

    assertThat(argumentDTO.getValue().getAlertId()).isNotNull();
  }

  @Test
  public void shouldReturnAnAlertWhenAlertExists() {
    UUID alertId = TestUtils.randomUUID();
    LiveAlertDTO dto = LiveAlertTestObjects
        .getAlertDTO(alertId, "alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
            "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
            "Dave Jones");
    when(mockRepository.findById(alertId)).thenReturn(Optional.ofNullable(dto));
    Optional<Alert> result = service.getAlertById(alertId.toString());

    assertThat(result.isPresent()).isTrue();
    assertThat(result.get().getAlertId()).isEqualTo(alertId.toString());
  }

  @Test
  public void shouldNotReturnAnAlertWhenAlertDOesNotExist() {
    UUID alertId = TestUtils.randomUUID();
    when(mockRepository.findById(alertId)).thenReturn(Optional.ofNullable(null));
    Optional<Alert> result = service.getAlertById(alertId.toString());

    assertThat(result.isPresent()).isFalse();
  }

  @Test
  public void shouldReturnEmptyWhenNoLiveAlertsFound() {
    List<Alert> alerts = service.getAllAlerts();
    assertThat(alerts).isEmpty();
  }

  @Test
  public void shouldReturnAlertsWhenLiveAlertsFound() {
    List<LiveAlertDTO> dtoList = LiveAlertTestObjects.getAlertDTOList();
    when(mockRepository.findAll()).thenReturn(dtoList);
    List<Alert> alerts = service.getAllAlerts();
    assertThat(alerts).hasSize(dtoList.size());
  }


  @Test
  public void shouldThrowExceptionWhenAlertNotFoundForUpdateAssignee() {
    UUID alertId = TestUtils.randomUUID();
    when(mockRepository.findById(alertId)).thenReturn(Optional.ofNullable(null));
    try {
      service.updateAssignee(alertId.toString(), new UpdateAssigneeRequest(), USER);
      fail("Expected EntityNotFoundException");
    } catch (EntityNotFoundException e) {
      assertThat(e.getMessage()).contains(alertId.toString());
    }

  }

  @Test
  public void shouldUpdateDataWhenAlertFoundForUpdateAssignee() {
    UUID alertId = TestUtils.randomUUID();
    LiveAlertDTO dto = LiveAlertTestObjects
        .getAlertDTO(alertId, "alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
            "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
            "Dave Jones");
    when(mockRepository.findById(alertId)).thenReturn(Optional.ofNullable(dto));
    when(mockRepository.save(any(LiveAlertDTO.class))).thenReturn(dto);

    UpdateAssigneeRequest updateRequest = LiveAlertTestObjects
        .getUpdateAssigneeRequest(TestUtils.randomAlphanumeric(10));
    try {
      Alert savedAlert = service.updateAssignee(alertId.toString(), updateRequest, USER);

      InOrder inOrder = inOrder(mockAuditRepository, mockRepository);
      inOrder.verify(mockAuditRepository, times(1)).save(argumentAuditDTO.capture());
      inOrder.verify(mockRepository, times(1)).save(argumentDTO.capture());

      assertThat(argumentAuditDTO.getValue()).isNotNull();
      assertThat(argumentAuditDTO.getValue().getPrimaryKey()).isNotNull();
      assertThat(argumentAuditDTO.getValue().getUpdatedBy()).isEqualTo(USER);
      assertThat(argumentAuditDTO.getValue().getUpdatedDate()).isNotNull();
      assertThat(argumentAuditDTO.getValue().getCreatedDate()).isNotNull();
      assertThat(argumentAuditDTO.getValue().getCreatedDate()).isEqualTo(dto.getCreatedDate());

      assertThat(argumentAuditDTO.getValue().getPrimaryKey().getAlertId())
          .isEqualTo(argumentDTO.getValue().getAlertId());
      assertThat(argumentAuditDTO.getValue().getAssignee())
          .isEqualTo(argumentDTO.getValue().getAssignee());

      assertThat(argumentDTO.getValue().getAssignee()).isEqualTo(updateRequest.getAssignee());
      assertThat(argumentDTO.getValue().getUpdatedDate()).isNotNull();
      assertThat(argumentDTO.getValue().getCreatedDate()).isNotNull();
      assertThat(argumentDTO.getValue().getCreatedDate()).isEqualTo(dto.getCreatedDate());
      assertThat(argumentDTO.getValue().getUpdatedBy()).isEqualTo(USER);


    } catch (EntityNotFoundException e) {
      fail("Not Expected: EntityNotFoundException");
    }

  }

  @Test
  public void shouldThrowExceptionWhenAlertNotFoundForUpdateAlert() {
    UUID alertId = TestUtils.randomUUID();
    when(mockRepository.findById(alertId)).thenReturn(Optional.ofNullable(null));
    try {
      service.updateAlert(alertId.toString(), new UpdateStateRequest(), USER);
      fail("Expected EntityNotFoundException");
    } catch (EntityNotFoundException | UpdateStateReasonMissingException e) {
      assertThat(e.getMessage()).contains(alertId.toString());
    }

  }

  @Test(expected = UpdateStateReasonMissingException.class)
  public void shouldThrowExceptionWhenMandatoryReasonNotFoundForUpdateAlert() throws Throwable {
    UpdateStateRequest updateRequest = LiveAlertTestObjects
        .getUpdateStateRequest("CLOSED", null);
    service.updateAlert(alertId.toString(), updateRequest, USER);
  }

  @Test(expected = UpdateStateReasonMissingException.class)
  public void shouldThrowExceptionWhenMandatoryReasonNotFoundForReopeningAnAlert()
      throws Throwable {
    LiveAlertDTO closedAlert = LiveAlertTestObjects
        .getAlertDTO(alertId, "alert 1", "CLOSED", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
            "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
            "Dave Jones");
    when(mockRepository.findById(alertId)).thenReturn(Optional.ofNullable(closedAlert));
    UpdateStateRequest updateRequest = LiveAlertTestObjects
        .getUpdateStateRequest("OPEN", null);
    service.updateAlert(alertId.toString(), updateRequest, USER);
  }

  @Test
  public void shouldNotThrowExceptionWhenReasonNotFoundForArchivingAnAlert()
      throws Throwable {
    LiveAlertDTO closedAlert = LiveAlertTestObjects
        .getAlertDTO(alertId, "alert 1", "CLOSED", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
            "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
            "Dave Jones");
    when(mockRepository.findById(alertId)).thenReturn(Optional.ofNullable(closedAlert));
    UpdateStateRequest updateRequest = LiveAlertTestObjects
        .getUpdateStateRequest("ARCHIVED", null);
    service.updateAlert(alertId.toString(), updateRequest, USER);
  }

  @Test(expected = UpdateStateReasonMissingException.class)
  public void shouldThrowExceptionWhenMandatoryReasonNotFoundForReopeningAnArchivedAlert()
      throws Throwable {
    LiveAlertDTO archivedAlert = LiveAlertTestObjects
        .getAlertDTO(alertId, "alert 1", "ARCHIVED", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
            "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
            "Dave Jones");
    when(mockRepository.findById(alertId)).thenReturn(Optional.ofNullable(archivedAlert));
    UpdateStateRequest updateRequest = LiveAlertTestObjects
        .getUpdateStateRequest("OPEN", null);
    service.updateAlert(alertId.toString(), updateRequest, USER);
  }


  @Test
  public void shouldUpdateDataWhenAlertFoundForUpdateAlert() {
    UpdateStateRequest updateRequest = LiveAlertTestObjects
        .getUpdateStateRequest("OPEN", null);
    try {
      Alert savedAlert = service.updateAlert(alertId.toString(), updateRequest, USER);
      assertThat(savedAlert.getAlertId()).isEqualTo(dto.getAlertId().toString());

      InOrder inOrder = inOrder(mockAuditRepository, mockRepository);
      inOrder.verify(mockAuditRepository, times(1)).save(argumentAuditDTO.capture());
      inOrder.verify(mockRepository, times(1)).save(argumentDTO.capture());

      assertThat(argumentAuditDTO.getValue()).isNotNull();
      assertThat(argumentAuditDTO.getValue().getPrimaryKey()).isNotNull();
      assertThat(argumentAuditDTO.getValue().getUpdatedBy()).isNotNull();
      assertThat(argumentAuditDTO.getValue().getUpdatedBy()).isEqualTo(USER);
      assertThat(argumentAuditDTO.getValue().getUpdatedDate()).isNotNull();
      assertThat(argumentAuditDTO.getValue().getState())
          .isEqualTo(argumentDTO.getValue().getState());

      assertThat(argumentAuditDTO.getValue().getPrimaryKey().getAlertId())
          .isEqualTo(argumentDTO.getValue().getAlertId());

      assertThat(argumentDTO.getValue().getState()).isEqualTo(updateRequest.getState().toString());
      assertThat(argumentDTO.getValue().getAlertId()).isEqualTo(dto.getAlertId());
      assertThat(argumentDTO.getValue().getUpdatedBy()).isNotNull();
      assertThat(argumentDTO.getValue().getUpdatedBy()).isEqualTo(USER);
      assertThat(argumentDTO.getValue().getUpdatedDate()).isNotNull();

    } catch (EntityNotFoundException | UpdateStateReasonMissingException e) {
      fail("Not Expected: EntityNotFoundException");
    }

  }


  @Test
  public void shouldSaveWhenStatusChangesFromOpenToCloseWithReasonForUpdateState() {
    UpdateStateRequest updateRequest = LiveAlertTestObjects
        .getUpdateStateRequest("CLOSED", "Test comment");

    try {
      service.updateAlert(alertId.toString(), updateRequest, USER);
      verify(mockRepository, times(1)).save(argumentDTO.capture());
      assertThat(argumentDTO.getValue().getState()).isEqualTo(updateRequest.getState().toString());
    } catch (EntityNotFoundException | UpdateStateReasonMissingException e) {
      fail("Not Expected: EntityNotFoundException");
    }

  }

  @Test
  public void shouldSaveWhenStatusChangesFromOpenToArchivedWithReasonForUpdateState() {
    UpdateStateRequest updateRequest = LiveAlertTestObjects
        .getUpdateStateRequest("ARCHIVED", "Test comment");

    try {
      service.updateAlert(alertId.toString(), updateRequest, USER);
      verify(mockRepository, times(1)).save(argumentDTO.capture());
      assertThat(argumentDTO.getValue().getState()).isEqualTo(updateRequest.getState().toString());
    } catch (EntityNotFoundException | UpdateStateReasonMissingException e) {
      fail("Not Expected: EntityNotFoundException");
    }

  }

  @Test
  public void shouldSaveWhenStatusChangesFromClosedToOpenWithReasonForUpdateState() {
    UpdateStateRequest updateRequest = LiveAlertTestObjects
        .getUpdateStateRequest("OPEN", "Test comment");

    try {
      service.updateAlert(alertId.toString(), updateRequest, USER);
      verify(mockRepository, times(1)).save(argumentDTO.capture());
      assertThat(argumentDTO.getValue().getState()).isEqualTo(updateRequest.getState().toString());
    } catch (EntityNotFoundException | UpdateStateReasonMissingException e) {
      fail("Not Expected: EntityNotFoundException");
    }

  }

  @Test
  public void shouldSaveWhenStatusChangesFromArchivedToOpenWithReasonForUpdateState() {
    UpdateStateRequest updateRequest = LiveAlertTestObjects
        .getUpdateStateRequest("OPEN", "Test comment");

    try {
      service.updateAlert(alertId.toString(), updateRequest, USER);
      verify(mockRepository, times(1)).save(argumentDTO.capture());
      assertThat(argumentDTO.getValue().getState()).isEqualTo(updateRequest.getState().toString());
    } catch (EntityNotFoundException | UpdateStateReasonMissingException e) {
      fail("Not Expected: EntityNotFoundException");
    }

  }

  @Test
  public void shouldSaveWhenStatusChangesFromClosedToArchivedWithReasonForUpdateState() {
    UpdateStateRequest updateRequest = LiveAlertTestObjects
        .getUpdateStateRequest("ARCHIVED", "Test comment");

    try {
      service.updateAlert(alertId.toString(), updateRequest, USER);
      verify(mockRepository, times(1)).save(argumentDTO.capture());
      assertThat(argumentDTO.getValue().getState()).isEqualTo(updateRequest.getState().toString());
    } catch (EntityNotFoundException | UpdateStateReasonMissingException e) {
      fail("Not Expected: EntityNotFoundException");
    }

  }


  @Test
  public void shouldSaveWhenStatusChangesFromOpenToClosedWithNoReasonForUpdateState() {
    UpdateStateRequest updateRequest = LiveAlertTestObjects
        .getUpdateStateRequest("OPEN", null);

    try {
      service.updateAlert(alertId.toString(), updateRequest, USER);
    } catch (EntityNotFoundException | UpdateStateReasonMissingException e) {
      assertThat(e.getMessage())
          .isEqualTo("A comment is required for changing the state of an alert : " + alertId);
    }

  }

  @Test
  public void shouldThrowExceptionWhenAlertNotFoundForGetAudit() {
    String alertId = TestUtils.randomUUID().toString();
    try {
      service.getAudit(alertId);
      fail("Cannot return audit history for non-existent alert");
    } catch (EntityNotFoundException e) {
      assertThat(e.getMessage()).contains(alertId);
    }
  }

  @Test
  public void shouldReturnDataWhenAuditFoundForgetAudit() {
    List<LiveAlertAuditDTO> alertAuditDTOS = LiveAlertTestObjects.getAlertAuditDTOs();
    when(mockAuditRepository.findAllByPrimaryKeyAlertId(alertId))
        .thenReturn(alertAuditDTOS);

    try {
      List<AlertAudit> alertAudits = service.getAudit(alertId.toString());
      assertThat(alertAudits).isNotNull();
      assertThat(alertAudits).hasSize(alertAuditDTOS.size());
    } catch (EntityNotFoundException e) {
      fail("Not Expected: EntityNotFoundException");
    }
  }
}
