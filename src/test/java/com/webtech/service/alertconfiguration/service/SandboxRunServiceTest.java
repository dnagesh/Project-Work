package com.webtech.service.alertconfiguration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alertconfiguration.TestObjects;
import com.webtech.service.alertconfiguration.dto.SandboxRunAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxRunDTO;
import com.webtech.service.alertconfiguration.dto.SandboxRunDTOPrimaryKey;
import com.webtech.service.alertconfiguration.exception.SandboxRunNotFoundException;
import com.webtech.service.alertconfiguration.mapper.CreateSandboxRunRequestMapper;
import com.webtech.service.alertconfiguration.mapper.SandboxRunDTOMapper;
import com.irisium.service.alertconfiguration.model.CreateSandboxRunRequest;
import com.irisium.service.alertconfiguration.model.SandboxRun;
import com.webtech.service.alertconfiguration.repository.SandboxRunRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SandboxRunServiceTest {

  UUID sandboxId;
  @Mock
  private SandboxRunRepository mockRepository;
  private SandboxRunService sandboxRunService;
  @Mock
  private SandboxAlertConfigurationService mockSandboxAlertConfigurationService;
  private SandboxRunDTO dto;
  @Captor
  private ArgumentCaptor<SandboxRunDTO> argumentDto;

  @Before
  public void setup() {
    sandboxRunService = new SandboxRunService(mockRepository, new SandboxRunDTOMapper(),
        new CreateSandboxRunRequestMapper(), mockSandboxAlertConfigurationService);
    sandboxId = TestUtils.randomUUID();
    dto = TestObjects.getSandboxRunDTO(sandboxId);
  }

  @Test
  public void shouldReturnEmptyWhenSandBoxIdNotExistsForGetAllRunsBySandboxId() {
    List<SandboxRun> sandboxes = sandboxRunService
        .getAllRunsBySandboxId(dto.getPrimaryKey().getSandboxUUID().toString());
    assertThat(sandboxes).isEmpty();
  }

  @Test
  public void shouldReturnDataWhenSandboxIdExistsForGetAllRunsBySandboxId() {
    List<SandboxRunDTO> dtoList = TestObjects.getSanboxRunsBySandboxIdDTOList(sandboxId);
    when(mockRepository.findAllByPrimaryKeySandboxUUID(sandboxId)).thenReturn(dtoList);
    List<SandboxRun> sandboxes = sandboxRunService.getAllRunsBySandboxId(sandboxId.toString());
    assertThat(sandboxes).hasSize(dtoList.size());
  }


  @Test
  public void shouldReturnSandboxRunWhenRunIdExistsForGetSandboxRunByRunId() {

    when(mockRepository.findById(dto.getPrimaryKey())).thenReturn(Optional.of(dto));
    Optional<SandboxRun> result = sandboxRunService
        .getSandboxRunByRunId(dto.getPrimaryKey().getSandboxUUID().toString(),
            dto.getPrimaryKey().getRunUUID().toString());
    assertThat(result.get().getCreatedBy()).isEqualTo(dto.getOwner());
    assertThat(result.get().getAlertConfigurations())
        .hasSize(dto.getAlertConfigurationSet().size());
    assertThat(result.get().getSandboxId())
        .isEqualTo(dto.getPrimaryKey().getSandboxUUID().toString());
    assertThat(result.get().getId()).isEqualTo(dto.getPrimaryKey().getRunUUID().toString());
    assertThat(result.get().getDataFromTime()).isEqualTo(dto.getDataFrom());
    assertThat(result.get().getDataToTime()).isEqualTo(dto.getDataTo());
    assertThat(result.get().getRunStartTime()).isEqualTo(dto.getStartTime());
    assertThat(result.get().getRunEndTime()).isEqualTo(dto.getEndTime());

  }

  @Test
  public void shouldReturnNullWhenRunIdNotExistsForGetSandboxRunByRunId() {
    Optional<SandboxRun> result = sandboxRunService
        .getSandboxRunByRunId(dto.getPrimaryKey().getSandboxUUID().toString(),
            dto.getPrimaryKey().getRunUUID().toString());
    assertThat(result.isPresent()).isFalse();
  }


  @Test
  public void shouldReturnSandboxRunWhenValidInputGivenForCreateSandboxRun() {

    Set<SandboxRunAlertConfigurationDTO> dtoSet = new HashSet<>();
    dtoSet.add(TestObjects.getSandboxRunAlertConfigurationDTO(TestUtils.randomUUID()));
    dtoSet.add(TestObjects.getSandboxRunAlertConfigurationDTO(TestUtils.randomUUID()));
    dtoSet.add(TestObjects.getSandboxRunAlertConfigurationDTO(TestUtils.randomUUID()));
    dtoSet.add(TestObjects.getSandboxRunAlertConfigurationDTO(TestUtils.randomUUID()));
    dtoSet.add(TestObjects.getSandboxRunAlertConfigurationDTO(TestUtils.randomUUID()));

    CreateSandboxRunRequest createSandboxRunRequest = new CreateSandboxRunRequest();
    ArrayList<String> configIds = new ArrayList<>();
    dtoSet.forEach(e -> configIds.add(e.getSandboxAlertConfigurationUUID().toString()));

    createSandboxRunRequest.setAlertConfigurationIds(configIds);
    String sandboxId = TestUtils.randomUUID().toString();
    when(mockSandboxAlertConfigurationService.getAllSandboxAlertConfigurationBySandboxId(sandboxId))
        .thenReturn(dtoSet);
    SandboxRun result = sandboxRunService
        .createSandboxRun(sandboxId, createSandboxRunRequest, "User1");
    assertThat(result.getRunStartTime()).isNotNull();
    assertThat(result.getCreatedBy()).isEqualTo("User1");
    assertThat(result.getSandboxId()).isEqualTo(sandboxId);
    assertThat(result.getId()).isNotNull();

  }

  @Test
  public void shouldUpdateSandboxRunWhenRunIdExistsForUpdateSandboxRunWhenFinished()
      throws SandboxRunNotFoundException {

    String sandboxId = TestUtils.randomUUID().toString();
    String sandboxRunId = TestUtils.randomUUID().toString();
    SandboxRunDTOPrimaryKey primaryKey = new SandboxRunDTOPrimaryKey();
    primaryKey.setSandboxUUID(UUID.fromString(sandboxId));
    primaryKey.setRunUUID(UUID.fromString(sandboxRunId));
    SandboxRunDTO sandboxRunDTO = new SandboxRunDTO();
    sandboxRunDTO.setPrimaryKey(primaryKey);
    when(mockRepository.findById(primaryKey)).thenReturn(Optional.of(sandboxRunDTO));
    sandboxRunService.updateSandboxRunWhenFinished(sandboxId, sandboxRunId);
    verify(mockRepository, times(1)).save(argumentDto.capture());
  }

  @Test(expected = SandboxRunNotFoundException.class)
  public void shouldThrowSandboxRunNotFoundExceptionWhenRunIdNotExistsForUpdateSandboxRunWhenFinished()
      throws SandboxRunNotFoundException {
    String sandboxId = TestUtils.randomUUID().toString();
    String sandboxRunId = TestUtils.randomUUID().toString();
    sandboxRunService.updateSandboxRunWhenFinished(sandboxId, sandboxRunId);
  }

  @Test
  public void shouldReturnEmptyResultIfNoRunsIdsFoundForGetAllRunIdsBySandboxId() {
    List<UUID> runIds = sandboxRunService
        .getAllRunIdsBySandboxId(TestUtils.randomUUID().toString());
    verify(mockRepository, times(1)).findAllByPrimaryKeySandboxUUID(any());
    assertThat(runIds).isNotNull();
    assertThat(runIds).isEmpty();
  }

  @Test
  public void shouldReturnResultIfRunsIdsFoundForGetAllRunIdsBySandboxId() {
    List<SandboxRunDTO> dtoList = TestObjects.getSanboxRunsBySandboxIdDTOList(sandboxId);
    when(mockRepository.findAllByPrimaryKeySandboxUUID(sandboxId)).thenReturn(dtoList);
    List<UUID> runIds = sandboxRunService.getAllRunIdsBySandboxId(sandboxId.toString());
    verify(mockRepository, times(1)).findAllByPrimaryKeySandboxUUID(any());
    assertThat(runIds).isNotNull();
    assertThat(runIds).isNotEmpty();
    assertThat(runIds).hasSize(runIds.size());
  }
}
