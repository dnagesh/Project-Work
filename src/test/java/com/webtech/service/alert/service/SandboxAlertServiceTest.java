package com.webtech.service.alert.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alert.SandboxAlertTestObjects;
import com.webtech.service.alert.dto.SandboxAlertAuditDTO;
import com.webtech.service.alert.dto.SandboxAlertDTO;
import com.webtech.service.alert.mapper.SandboxAlertObjectMapper;
import com.webtech.service.alert.repository.SandboxAlertAuditDTORepository;
import com.webtech.service.alert.repository.SandboxAlertDTORepository;
import com.webtech.service.alertconfiguration.service.SandboxRunService;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.irisium.service.sandboxalert.model.CreateSandboxAlertRequest;
import com.irisium.service.sandboxalert.model.SandboxAlert;
import com.irisium.service.sandboxalert.model.SandboxAlertAudit;
import com.irisium.service.sandboxalert.model.UpdateSandboxAlertAssigneeRequest;
import com.irisium.service.sandboxalert.model.UpdateSandboxAlertStateRequest;
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
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SandboxAlertServiceTest {

  private static final String USER = "User";
  UUID alertId;
  UUID runId;
  private SandboxAlertService service;
  @Mock
  private SandboxAlertDTORepository mockAlertRepository;
  @Mock
  private SandboxAlertAuditDTORepository mockAuditRepository;
  @Mock
  private SandboxRunService sandboxRunService;
  private SandboxAlertObjectMapper objectMapper;
  @Captor
  private ArgumentCaptor<SandboxAlertDTO> argumentDTO;
  @Captor
  private ArgumentCaptor<SandboxAlertAuditDTO> argumentAuditDTO;
  private CreateSandboxAlertRequest createAlertRequest;
  private SandboxAlertDTO alertDTO;

  @Before
  public void setUp() throws Exception {
    alertId = TestUtils.randomUUID();
    runId = TestUtils.randomUUID();
    objectMapper = new SandboxAlertObjectMapper();
    service = new SandboxAlertService(mockAlertRepository, mockAuditRepository, objectMapper,
        sandboxRunService);
    createAlertRequest = SandboxAlertTestObjects
        .getAlertCreateRequest(runId.toString(), "alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", Arrays.asList("Eleis Commodities"), "Wash Trade",
            "Europe/Equity", Arrays.asList("Regulatory", "Operational"));
    alertDTO = SandboxAlertTestObjects
        .getAlertDTO(alertId, runId, "alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
            "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
            "Dave Jones");

    when(mockAlertRepository.findById(any())).thenReturn(Optional.ofNullable(alertDTO));
    when(mockAlertRepository.save(ArgumentMatchers.any(SandboxAlertDTO.class)))
        .thenReturn(alertDTO);
  }


  @Test
  public void shouldCreateAlertWhenValidData() {

    SandboxAlert alert = service.createSandboxAlert(createAlertRequest);

    InOrder inOrder = inOrder(mockAuditRepository, mockAlertRepository);
    inOrder.verify(mockAuditRepository, times(1)).save(argumentAuditDTO.capture());
    inOrder.verify(mockAlertRepository, times(1)).save(argumentDTO.capture());

    assertThat(argumentAuditDTO.getValue()).isNotNull();
    assertThat(argumentAuditDTO.getValue().getPrimaryKey()).isNotNull();
    assertThat(argumentAuditDTO.getValue().getPrimaryKey().getAlertId()).isNotNull();
    assertThat(argumentAuditDTO.getValue().getPrimaryKey().getAuditId()).isNotNull();
    assertThat(argumentAuditDTO.getValue().getState()).isEqualTo(alertDTO.getState());

    assertThat(argumentDTO.getValue().getState())
        .isEqualTo(createAlertRequest.getState().toString());
    assertThat(argumentDTO.getValue().getType()).isEqualTo(createAlertRequest.getType());
    assertThat(argumentDTO.getValue().getConfiguration())
        .isEqualTo(createAlertRequest.getConfiguration());

    assertThat(argumentDTO.getValue().getPrimaryKey().getAlertId())
        .isEqualTo(argumentAuditDTO.getValue().getPrimaryKey().getAlertId());
    assertThat(argumentDTO.getValue().getPrimaryKey().getRunId().toString())
        .isEqualTo(createAlertRequest.getRunId());
  }

  @Test
  public void shouldReturnAnAlertWhenGetAlertByValidId() throws Throwable {
    SandboxAlert result = service.getSandboxAlertByIdAndRunId(alertId.toString(), runId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getAlertId()).isEqualTo(alertId.toString());
    assertThat(result.getRunId()).isEqualTo(runId.toString());
  }

  @Test(expected = EntityNotFoundException.class)
  public void shouldThrowExceptionWhenAlertNotFound() throws Throwable {
    when(mockAlertRepository.findById(any())).thenReturn(Optional.ofNullable(null));
    SandboxAlert result = service.getSandboxAlertByIdAndRunId(alertId.toString(), runId.toString());
  }

  @Test
  public void shouldReturnEmptyWhenNoResultsForGetAllAlerts() {
    List<SandboxAlert> alerts = service
        .getAllSandboxAlerts(TestUtils.randomUUID().toString(), runId.toString());
    assertThat(alerts).isEmpty();
  }

  @Test
  public void shouldReturnDataWhenResultsFoundForGetAllAlerts() {
    List<SandboxAlertDTO> dtoList = SandboxAlertTestObjects.getAlertDTOList();
    when(mockAlertRepository.findAll()).thenReturn(dtoList);
    List<SandboxAlert> alerts = service
        .getAllSandboxAlerts(null, null);
    assertThat(alerts).hasSize(dtoList.size());
  }

  @Test
  public void shouldReturnDataWhenResultsFoundForGetAllAlertsForSandbox() {
    List<UUID> runIds = Arrays.asList(TestUtils.randomUUID(), TestUtils.randomUUID());
    when(sandboxRunService.getAllRunIdsBySandboxId(any())).thenReturn(runIds);
    List<SandboxAlertDTO> dtoList = SandboxAlertTestObjects.getAlertDTOList();
    when(mockAlertRepository.findAllByRunIds(runIds)).thenReturn(dtoList);
    List<SandboxAlert> alerts = service
        .getAllSandboxAlerts(TestUtils.randomUUID().toString(), null);
    assertThat(alerts).hasSize(dtoList.size());
  }

  @Test
  public void shouldReturnDataWhenResultsFoundForGetAllAlertsByRunId() {
    List<SandboxAlertDTO> dtoList = SandboxAlertTestObjects.getAlertDTOList();
    when(mockAlertRepository.findAllByPrimaryKeyRunId(runId)).thenReturn(dtoList);
    List<SandboxAlert> alerts = service
        .getAllSandboxAlerts(null, runId.toString());
    assertThat(alerts).hasSize(dtoList.size());
  }


  @Test
  public void shouldThrowExceptionWhenAlertNotFoundForUpdateAssignee() {
    UUID alertId = TestUtils.randomUUID();
    when(mockAlertRepository.findById(any())).thenReturn(Optional.ofNullable(null));
    try {
      service.updateAssignee(alertId.toString(), runId.toString(),
          new UpdateSandboxAlertAssigneeRequest(), USER);
      fail("Expected EntityNotFoundException");
    } catch (EntityNotFoundException e) {
      assertThat(e.getMessage()).contains(alertId.toString());
      assertThat(e.getMessage()).contains(runId.toString());
    }

  }

  @Test
  public void shouldUpdateDataWhenAlertFoundForUpdateAssignee() {

    SandboxAlertDTO savedDTO = SandboxAlertTestObjects
        .getAlertDTO(alertId, runId, "alert 1", "LINKED", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
            "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
            "Dave Jones");
    when(mockAlertRepository.save(any(SandboxAlertDTO.class))).thenReturn(savedDTO);

    UpdateSandboxAlertAssigneeRequest updateRequest = SandboxAlertTestObjects
        .getUpdateAssigneeRequest(TestUtils.randomAlphanumeric(10));
    try {
      SandboxAlert savedAlert = service
          .updateAssignee(alertId.toString(), runId.toString(), updateRequest, USER);
      assertThat(savedAlert.getAlertId())
          .isEqualTo(alertDTO.getPrimaryKey().getAlertId().toString());
      assertThat(savedAlert.getRunId()).isEqualTo(alertDTO.getPrimaryKey().getRunId().toString());

      InOrder inOrder = inOrder(mockAuditRepository, mockAlertRepository);
      inOrder.verify(mockAuditRepository, times(1)).save(argumentAuditDTO.capture());
      inOrder.verify(mockAlertRepository, times(1)).save(argumentDTO.capture());

      assertThat(argumentAuditDTO.getValue()).isNotNull();
      assertThat(argumentAuditDTO.getValue().getPrimaryKey()).isNotNull();
      assertThat(argumentAuditDTO.getValue().getPrimaryKey().getAlertId())
          .isEqualTo(argumentDTO.getValue().getPrimaryKey().getAlertId());
      assertThat(argumentAuditDTO.getValue().getRunId())
          .isEqualTo(argumentDTO.getValue().getPrimaryKey().getRunId());
      assertThat(argumentAuditDTO.getValue().getPrimaryKey().getAuditId())
          .isNotNull();
      assertThat(argumentAuditDTO.getValue().getState())
          .isEqualTo(argumentDTO.getValue().getState());

      assertThat(argumentDTO.getValue().getAssignee()).isEqualTo(updateRequest.getAssignee());
      assertThat(argumentDTO.getValue().getUpdatedBy()).isEqualTo(USER);
      assertThat(argumentDTO.getValue().getUpdatedDate()).isNotNull();

    } catch (EntityNotFoundException e) {
      fail("Not Expected: EntityNotFoundException");
    }

  }

  @Test
  public void shouldThrowExceptionWhenAlertNotFoundForUpdateAlert() {
    UUID alertId = TestUtils.randomUUID();
    when(mockAlertRepository.findById(any())).thenReturn(Optional.ofNullable(null));
    try {
      service
          .updateAlert(alertId.toString(), runId.toString(), new UpdateSandboxAlertStateRequest(),
              USER);
      fail("Expected EntityNotFoundException");
    } catch (EntityNotFoundException e) {
      assertThat(e.getMessage()).contains(alertId.toString());
      assertThat(e.getMessage()).contains(runId.toString());
      Mockito.verifyZeroInteractions(mockAuditRepository);
    }

  }

  @Test
  public void shouldUpdateDataWhenAlertFoundForUpdateAlert() {

    UpdateSandboxAlertStateRequest updateRequest = SandboxAlertTestObjects
        .getUpdateStateRequest("OPEN", null);
    try {
      SandboxAlert savedAlert = service
          .updateAlert(alertId.toString(), runId.toString(), updateRequest, USER);
      assertThat(savedAlert.getAlertId())
          .isEqualTo(alertDTO.getPrimaryKey().getAlertId().toString());

      InOrder inOrder = inOrder(mockAuditRepository, mockAlertRepository);
      inOrder.verify(mockAuditRepository, times(1)).save(argumentAuditDTO.capture());
      inOrder.verify(mockAlertRepository, times(1)).save(argumentDTO.capture());

      assertThat(argumentAuditDTO.getValue()).isNotNull();
      assertThat(argumentAuditDTO.getValue().getPrimaryKey()).isNotNull();
      assertThat(argumentAuditDTO.getValue().getUpdatedBy())
          .isEqualTo(USER);
      assertThat(argumentAuditDTO.getValue().getPrimaryKey().getAuditId())
          .isNotNull();
      assertThat(argumentAuditDTO.getValue().getState())
          .isEqualTo(argumentDTO.getValue().getState());
      assertThat(argumentAuditDTO.getValue().getUpdatedBy()).isEqualTo(USER);
      assertThat(argumentAuditDTO.getValue().getUpdatedDate()).isNotNull();

      assertThat(argumentAuditDTO.getValue().getPrimaryKey().getAlertId())
          .isEqualTo(argumentDTO.getValue().getPrimaryKey().getAlertId());
      assertThat(argumentAuditDTO.getValue().getRunId())
          .isEqualTo(argumentDTO.getValue().getPrimaryKey().getRunId());

      assertThat(argumentDTO.getValue().getState()).isEqualTo(updateRequest.getState().toString());
      assertThat(argumentDTO.getValue().getUpdatedBy()).isEqualTo(USER);
      assertThat(argumentDTO.getValue().getUpdatedDate()).isNotNull();

    } catch (EntityNotFoundException e) {
      fail("Not Expected: EntityNotFoundException");
    }

  }


  @Test(expected = EntityNotFoundException.class)
  public void shouldThrowExceptionWhenAlertNotFoundForGetAudit() throws Throwable {
    when(mockAlertRepository.findById(any())).thenReturn(Optional.empty());
    service.getAuditForSandboxAlert(TestUtils.randomUUID().toString(),
        TestUtils.randomUUID().toString());
    fail("Cannot return audit history for non-existent alert");

  }

  @Test
  public void shouldReturnDataWhenAuditFoundForgetAudit() {

    List<SandboxAlertAuditDTO> alertAuditDTOS = SandboxAlertTestObjects.getAlertAuditDTOs();
    when(mockAuditRepository.findAllByPrimaryKeyAlertId(alertId))
        .thenReturn(alertAuditDTOS);

    try {
      List<SandboxAlertAudit> alertAudits = service
          .getAuditForSandboxAlert(alertId.toString(), runId.toString());
      assertThat(alertAudits).isNotNull();
      assertThat(alertAudits).hasSize(alertAuditDTOS.size());
    } catch (EntityNotFoundException e) {
      fail("Not Expected: EntityNotFoundException");
    }
  }

  @Test(expected = EntityNotFoundException.class)
  public void shouldThrowExceptionWhenAlertNotFoundForUpdatePromotedStatus() throws Throwable {
    when(mockAlertRepository.findById(any())).thenReturn(Optional.empty());
    SandboxAlert sandboxAlert = new SandboxAlert();
    sandboxAlert.setRunId(runId.toString());
    sandboxAlert.setAlertId(alertId.toString());
    service.promoteSandboxAlertToLive(sandboxAlert, USER);
    fail("Cannot promote non-existent SandboxAlert");

  }

  @Test
  public void shouldReturnPromotedSandboxAlertWhenFoundForUpdatePromotedStatus() {
    try {
      SandboxAlert sandboxAlert = new SandboxAlert();
      sandboxAlert.setRunId(runId.toString());
      sandboxAlert.setAlertId(alertId.toString());
      SandboxAlert result = service.promoteSandboxAlertToLive(sandboxAlert, USER);

      assertThat(result.getAlertId())
          .isEqualTo(alertDTO.getPrimaryKey().getAlertId().toString());

      InOrder inOrder = inOrder(mockAuditRepository, mockAlertRepository);
      inOrder.verify(mockAuditRepository, times(1)).save(argumentAuditDTO.capture());
      inOrder.verify(mockAlertRepository, times(1)).save(argumentDTO.capture());

      assertThat(argumentDTO.getValue().isPromotedToLive()).isTrue();
      assertThat(argumentDTO.getValue().getUpdatedBy()).isEqualTo(USER);
      assertThat(argumentDTO.getValue().getUpdatedDate()).isNotNull();

      assertThat(argumentAuditDTO.getValue()).isNotNull();
      assertThat(argumentAuditDTO.getValue().getPrimaryKey()).isNotNull();
      assertThat(argumentAuditDTO.getValue().getPrimaryKey().getAuditId())
          .isNotNull();
      assertThat(argumentAuditDTO.getValue().isPromotedToLive())
          .isEqualTo(argumentDTO.getValue().isPromotedToLive());
      assertThat(argumentAuditDTO.getValue().getUpdatedBy()).isEqualTo(USER);
      assertThat(argumentAuditDTO.getValue().getUpdatedDate()).isNotNull();

      assertThat(argumentAuditDTO.getValue().getPrimaryKey().getAlertId())
          .isEqualTo(argumentDTO.getValue().getPrimaryKey().getAlertId());
      assertThat(argumentAuditDTO.getValue().getRunId())
          .isEqualTo(argumentDTO.getValue().getPrimaryKey().getRunId());

    } catch (EntityNotFoundException e) {
      fail("Not Expected: EntityNotFoundException");
    }
  }
}
