package com.webtech.service.alertconfiguration;


import com.irisium.TestUtils;
import com.irisium.service.alertDefinition.model.GuiDeploymentAlertType;
import com.irisium.service.alertDefinition.model.LogicOverride;
import com.irisium.service.alertDefinition.model.Parameter;
import com.webtech.service.alertconfiguration.dto.AlertFilterDTO;
import com.webtech.service.alertconfiguration.dto.AlertParameterSetDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByAlertConfigUUIDDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByMonthDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByMonthDTOPrimaryKey;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditByMonthDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditByMonthDTOPrimaryKey;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditDTOPrimaryKey;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTOPrimaryKey;
import com.webtech.service.alertconfiguration.dto.SandboxDTO;
import com.webtech.service.alertconfiguration.dto.SandboxRunAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxRunDTO;
import com.webtech.service.alertconfiguration.dto.SandboxRunDTOPrimaryKey;
import com.irisium.service.alertconfiguration.model.AlertFilter;
import com.irisium.service.alertconfiguration.model.AlertFilter.OperationEnum;
import com.irisium.service.alertconfiguration.model.CreateUpdateSandboxAlertConfigRequest;
import com.irisium.service.alertconfiguration.model.EntityLinkRequest;
import com.irisium.service.alertconfiguration.model.EntityRelationship;
import com.irisium.service.alertconfiguration.model.EntityRelationshipAudit;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration.StatusEnum;
import com.irisium.service.alertconfiguration.model.LiveAlertConfigurationAudit;
import com.irisium.service.alertconfiguration.model.Sandbox;
import com.irisium.service.alertconfiguration.model.SandboxAlertConfiguration;
import com.irisium.service.alertconfiguration.model.SandboxAlertConfigurationAudit;
import com.irisium.service.alertconfiguration.model.SandboxRun;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.entityrelationship.model.Relationship;
import com.webtech.service.entityrelationship.model.RelationshipAudit;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestObjects {


  public static List<SandboxDTO> getSanboxesDTOList() {
    List<SandboxDTO> dtoList = new ArrayList<>();
    dtoList.add(getSandboxDTO());
    dtoList.add(getSandboxDTO());
    dtoList.add(getSandboxDTO(TestUtils.randomUUID(), TestUtils.randomAlphanumeric(10),
        TestUtils.randomAlphanumeric(10), Instant.now(), StatusEnum.ACTIVE.toString()));
    return dtoList;
  }

  public static List<SandboxRunDTO> getSanboxRunsBySandboxIdDTOList(UUID sandboxId) {
    List<SandboxRunDTO> dtoList = new ArrayList<>();
    dtoList.add(getSandboxRunDTO(sandboxId));
    dtoList.add(getSandboxRunDTO(sandboxId));
    return dtoList;
  }

  public static SandboxDTO getSandboxDTO() {
    SandboxDTO dto = new SandboxDTO();
    dto.setUuid(TestUtils.randomUUID());
    dto.setName(TestUtils.randomAlphanumeric(10));
    dto.setOwner(TestUtils.randomAlphanumeric(10));
    dto.setCreatedWhen(Instant.now());
    dto.setStatus(TestUtils.randomEnum(Sandbox.StatusEnum.class).toString());
    return dto;
  }

  public static SandboxRunDTO getSandboxRunDTO(UUID sandboxId) {
    SandboxRunDTO dto = new SandboxRunDTO();
    SandboxRunDTOPrimaryKey primaryKey = new SandboxRunDTOPrimaryKey();
    primaryKey.setRunUUID(TestUtils.randomUUID());
    primaryKey.setSandboxUUID(sandboxId);
    dto.setPrimaryKey(primaryKey);
    dto.setAlertConfigurationSet(new HashSet<>());
    dto.setOwner(TestUtils.randomAlphanumeric(10));
    dto.setDataFrom(TestUtils.randomInstant());
    dto.setDataTo(TestUtils.randomInstant());
    dto.setEndTime(TestUtils.randomInstant());
    dto.setStartTime(TestUtils.randomInstant());
    return dto;
  }

  public static List<Sandbox> getSandboxesList() {
    List<Sandbox> dtoList = new ArrayList<>();
    dtoList.add(getSandbox());
    dtoList.add(getSandbox());
    return dtoList;
  }


  public static Sandbox getSandbox() {

    Sandbox sandbox = new Sandbox();
    sandbox.setId(TestUtils.randomUUID().toString());
    sandbox.setName(TestUtils.randomAlphanumeric(10));
    sandbox.setStatus(TestUtils.randomEnum(Sandbox.StatusEnum.class));
    sandbox.setCreatedWhen(Instant.now());
    sandbox.setCreatedBy(TestUtils.randomAlphanumeric(10));
    return sandbox;
  }

  public static SandboxRun getSandboxRun() {

    SandboxRun sandboxRun = new SandboxRun();
    sandboxRun.setId(TestUtils.randomUUID().toString());
    sandboxRun.setSandboxId(TestUtils.randomUUID().toString());
    sandboxRun.setAlertConfigurations(
        Arrays.asList(TestUtils.randomUUID().toString(), TestUtils.randomUUID().toString()));
    sandboxRun.setRunStartTime(TestUtils.randomInstant());
    sandboxRun.setRunEndTime(TestUtils.randomInstant());
    sandboxRun.setDataFromTime(TestUtils.randomInstant());
    sandboxRun.setDataToTime(TestUtils.randomInstant());
    sandboxRun.setCreatedBy(TestUtils.randomAlphanumeric(10));
    return sandboxRun;

  }


  public static SandboxDTO getSandboxDTO(UUID sandboxId, String name, String owner,
      Instant lastUpdated, String status) {
    SandboxDTO dto = new SandboxDTO();
    dto.setUuid(sandboxId);
    dto.setName(name);
    dto.setOwner(owner);
    dto.setCreatedWhen(lastUpdated);
    dto.setStatus(status);
    return dto;
  }

  public static SandboxRunAlertConfigurationDTO getSandboxRunAlertConfigurationDTO(UUID configId) {

    SandboxRunAlertConfigurationDTO dto = new SandboxRunAlertConfigurationDTO();
    dto.setSandboxAlertConfigurationUUID(configId);
    dto.setName(TestUtils.randomAscii());
    dto.setAppHash(TestUtils.randomAscii());
    return dto;

  }

  public static List<LiveAlertConfiguration> getLiveConfigurations() {
    List<LiveAlertConfiguration> liveAlertConfigurations = new ArrayList<>();
    liveAlertConfigurations.add(getLiveConfiguration(TestUtils.randomUUID().toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    liveAlertConfigurations.add(getLiveConfiguration(TestUtils.randomUUID().toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    return liveAlertConfigurations;
  }

  public static LiveAlertConfiguration getLiveConfiguration(String id, String name,
      String alertConfigType, String status, String createdBy, Instant createdWhen) {
    LiveAlertConfiguration alertConfiguration = new LiveAlertConfiguration();
    alertConfiguration.setAlertConfigurationUUID(id);
    alertConfiguration.setName(name);
    alertConfiguration.setAlertConfigType(alertConfigType);
    alertConfiguration.setStatus(StatusEnum.fromValue(status));
    alertConfiguration.setWhen(createdWhen);
    alertConfiguration.setWho(createdBy);
    return alertConfiguration;
  }

  public static List<LiveAlertConfigurationAudit> getLiveConfigurationAudits() {
    List<LiveAlertConfigurationAudit> liveAlertConfigurations = new ArrayList<>();
    liveAlertConfigurations.add(getLiveConfigurationAudit(TestUtils.randomUUID().toString(),
        TestUtils.randomUUID().toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        LiveAlertConfigurationAudit.StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    liveAlertConfigurations.add(getLiveConfigurationAudit(TestUtils.randomUUID().toString(),
        TestUtils.randomUUID().toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        LiveAlertConfigurationAudit.StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    return liveAlertConfigurations;
  }

  public static List<SandboxAlertConfigurationAudit> getSandboxConfigurationAudits() {
    List<SandboxAlertConfigurationAudit> sandboxAlertConfigurations = new ArrayList<>();
    sandboxAlertConfigurations.add(getSandboxConfigurationAudit(TestUtils.randomUUID().toString(),
        TestUtils.randomUUID().toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        LiveAlertConfigurationAudit.StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    sandboxAlertConfigurations.add(getSandboxConfigurationAudit(TestUtils.randomUUID().toString(),
        TestUtils.randomUUID().toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        LiveAlertConfigurationAudit.StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    return sandboxAlertConfigurations;
  }

  public static LiveAlertConfigurationAudit getLiveConfigurationAudit(String id,
      String alertConfigUUID, String name, String alertConfigType, String status, String createdBy,
      Instant createdWhen) {
    LiveAlertConfigurationAudit alertConfiguration = new LiveAlertConfigurationAudit();

    alertConfiguration.setAuditUUID(id);
    alertConfiguration.alertConfigurationUUID(alertConfigUUID);
    alertConfiguration.setName(name);
    alertConfiguration.setAlertConfigType(alertConfigType);
    alertConfiguration.setStatus(LiveAlertConfigurationAudit.StatusEnum.fromValue(status));
    alertConfiguration.setWhen(createdWhen);
    alertConfiguration.setWho(createdBy);
    return alertConfiguration;
  }

  public static SandboxAlertConfigurationAudit getSandboxConfigurationAudit(String id,
      String alertConfigUUID, String name, String alertConfigType, String status, String createdBy,
      Instant createdWhen) {
    SandboxAlertConfigurationAudit alertConfiguration = new SandboxAlertConfigurationAudit();
    alertConfiguration.setAuditUUID(id);
    alertConfiguration.alertConfigurationUUID(alertConfigUUID);
    alertConfiguration.setName(name);
    alertConfiguration.setAlertConfigType(alertConfigType);
    alertConfiguration.setStatus(SandboxAlertConfigurationAudit.StatusEnum.fromValue(status));
    alertConfiguration.setWhen(createdWhen);
    alertConfiguration.setWho(createdBy);
    return alertConfiguration;
  }

  public static LiveAlertConfigurationAuditByMonthDTO getLiveConfigurationAuditByMonthDTO(UUID id,
                                                                                          UUID alertConfigUUID, String name, String alertConfigType, String status, String createdBy,
                                                                                          Instant createdWhen) {
    LiveAlertConfigurationAuditByMonthDTO auditDTO = new LiveAlertConfigurationAuditByMonthDTO();

    LiveAlertConfigurationAuditByMonthDTOPrimaryKey primaryKey = new LiveAlertConfigurationAuditByMonthDTOPrimaryKey();
    primaryKey.setAuditUUID(id);
    primaryKey.setWhenMonth(YearMonth.now().atDay(1));
    auditDTO.setPrimaryKey(primaryKey);
    auditDTO.setAlertConfigurationUUID(alertConfigUUID);
    auditDTO.setName(name);
    auditDTO.setAlertLogicType(alertConfigType);
    auditDTO.setStatus(status);
    auditDTO.setCreatedWhen(createdWhen);
    auditDTO.setCreatedBy(createdBy);
    return auditDTO;
  }

  public static SandboxAlertConfigurationAuditByMonthDTO getSandboxConfigurationAuditByMonthDTO(
      UUID id,
      UUID alertConfigUUID, String name, String alertConfigType, String status, String createdBy,
      Instant createdWhen) {
    SandboxAlertConfigurationAuditByMonthDTO auditDTO = new SandboxAlertConfigurationAuditByMonthDTO();

    SandboxAlertConfigurationAuditByMonthDTOPrimaryKey primaryKey = new SandboxAlertConfigurationAuditByMonthDTOPrimaryKey(
        YearMonth.now().atDay(1), id, TestUtils.randomUUID());
    auditDTO.setPrimaryKey(primaryKey);
    auditDTO.setAlertConfigurationUUID(alertConfigUUID);
    auditDTO.setName(name);
    auditDTO.setAlertLogicType(alertConfigType);
    auditDTO.setStatus(status);
    auditDTO.setCreatedWhen(createdWhen);
    auditDTO.setCreatedBy(createdBy);
    return auditDTO;
  }

  public static List<LiveAlertConfigurationAuditByMonthDTO> getLiveConfigurationAuditByMonthDTOList() {
    List<LiveAlertConfigurationAuditByMonthDTO> auditDTOS = new ArrayList<>();
    auditDTOS.add(getLiveConfigurationAuditByMonthDTO(TestUtils.randomUUID(),
        TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    auditDTOS.add(getLiveConfigurationAuditByMonthDTO(TestUtils.randomUUID(),
        TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    return auditDTOS;
  }

  public static List<SandboxAlertConfigurationAuditByMonthDTO> getSandboxConfigurationAuditByMonthDTOList() {
    List<SandboxAlertConfigurationAuditByMonthDTO> auditDTOS = new ArrayList<>();
    auditDTOS.add(getSandboxConfigurationAuditByMonthDTO(TestUtils.randomUUID(),
        TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    auditDTOS.add(getSandboxConfigurationAuditByMonthDTO(TestUtils.randomUUID(),
        TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    return auditDTOS;
  }

  public static LiveAlertConfigurationDTO getLiveConfigurationDTO(UUID alertConfigUUID, String name,
      String alertConfigType, String status, Instant createdWhen, String createdBy,
      Instant updatedWhen, String updateddBy,
      String comment) {
    LiveAlertConfigurationDTO dto = new LiveAlertConfigurationDTO();
    dto.setUuid(alertConfigUUID);
    dto.setName(name);
    dto.setAlertLogicType(alertConfigType);
    dto.setStatus(status);
    dto.setCreatedWhen(createdWhen);
    dto.setCreatedBy(createdBy);
    dto.setUpdatedWhen(updatedWhen);
    dto.setUpdatedBy(updateddBy);
    dto.setComment(comment);
    return dto;
  }

  public static List<LiveAlertConfigurationDTO> getLiveConfigurationDTOList() {
    List<LiveAlertConfigurationDTO> liveAlertConfigurationDTOS = new ArrayList<>();
    liveAlertConfigurationDTOS.add(getLiveConfigurationDTO(TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5)
    ));
    liveAlertConfigurationDTOS.add(getLiveConfigurationDTO(TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5)
    ));
    return liveAlertConfigurationDTOS;
  }


  public static AlertParameterSetDTO getAlertParameterSetDTO() throws NoSuchAlgorithmException {
    Map<String, String> alertParameters = new HashMap<>();
    alertParameters.put(TestUtils.randomAlphanumeric(5), TestUtils.randomAlphanumeric(10));

    Set<String> alertAggregationFields = new HashSet<>(
        Arrays.asList(TestUtils.randomAlphanumeric(5), TestUtils.randomAlphanumeric(5)));

    Set<AlertFilterDTO> alertFilterDTOS = new HashSet<>();
    AlertFilterDTO alertFilterDTO = new AlertFilterDTO();
    alertFilterDTO.setOperation(TestUtils.randomAlphanumeric(2));
    alertFilterDTO.setAggregationFieldName(TestUtils.randomAlphanumeric(10));
    alertFilterDTO.setValues(new HashSet<>(
        Arrays.asList(TestUtils.randomAlphanumeric(5), TestUtils.randomAlphanumeric(5))));
    alertFilterDTOS.add(alertFilterDTO);

    String alertLogicType = TestUtils.randomAlphanumeric(10);
    String businessUnit = TestUtils.randomAlphanumeric(10);

    Map<String, Boolean> logicOverrides = new HashMap<>();
    logicOverrides.put("logic1", true);

    AlertParameterSetDTO dto = new AlertParameterSetDTO(alertLogicType, businessUnit,
        alertParameters, alertAggregationFields, alertFilterDTOS, logicOverrides);

    return dto;
  }

  public static List<LiveAlertConfigurationAuditByAlertConfigUUIDDTO> getLiveConfigurationAuditByAlertConfigUUIDDTOList() {
    List<LiveAlertConfigurationAuditByAlertConfigUUIDDTO> auditDTOS = new ArrayList<>();
    auditDTOS.add(getLiveConfigurationAuditByAlertConfigUUIDDTO(
        TestUtils.randomUUID(), TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    auditDTOS.add(getLiveConfigurationAuditByAlertConfigUUIDDTO(
        TestUtils.randomUUID(), TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    return auditDTOS;
  }

  public static LiveAlertConfigurationAuditByAlertConfigUUIDDTO getLiveConfigurationAuditByAlertConfigUUIDDTO(
      UUID alertConfigUUID, UUID auuditUUId, String name,
      String alertConfigType, String status, String createdBy, Instant createdWhen,
      String updatedBy, Instant updatedWhen) {
    LiveAlertConfigurationAuditByAlertConfigUUIDDTO alertConfiguration = new LiveAlertConfigurationAuditByAlertConfigUUIDDTO();

    LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey primaryKey = new LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey(
        alertConfigUUID, TestUtils.randomInstant());

    alertConfiguration.setPrimaryKey(primaryKey);
    alertConfiguration.setAuditUUID(auuditUUId);
    alertConfiguration.setName(name);
    alertConfiguration.setAlertLogicType(alertConfigType);
    alertConfiguration.setStatus(status);
    alertConfiguration.setCreatedWhen(createdWhen);
    alertConfiguration.setCreatedBy(createdBy);
    alertConfiguration.setCreatedWhen(updatedWhen);
    alertConfiguration.setCreatedBy(updatedBy);
    return alertConfiguration;
  }

  public static List<SandboxAlertConfigurationDTO> getSandboxConfigurationDTOList() {
    List<SandboxAlertConfigurationDTO> dtoList = new ArrayList<>();
    dtoList.add(getSandboxConfigurationDTO(TestUtils.randomUUID(),
        TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    dtoList.add(getSandboxConfigurationDTO(TestUtils.randomUUID(),
        TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    return dtoList;
  }

  public static List<SandboxAlertConfigurationAuditDTO> getSandboxConfigurationAuditDTOList() {
    List<SandboxAlertConfigurationAuditDTO> dtoList = new ArrayList<>();

    dtoList.add(getSandboxConfigurationAuditDTO(TestUtils.randomUUID(),
        TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    dtoList.add(getSandboxConfigurationAuditDTO(TestUtils.randomUUID(),
        TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    return dtoList;
  }

  public static SandboxAlertConfigurationDTO getSandboxConfigurationDTO(
      UUID randomUUID, UUID alertConfigUUID, String name,
      String alertConfigType, String status, String createdBy, Instant createdWhen,
      String updatedBy, Instant updatedWhen) {
    SandboxAlertConfigurationDTO alertConfiguration = new SandboxAlertConfigurationDTO();

    SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey(
        randomUUID, alertConfigUUID);

    alertConfiguration.setPrimaryKey(primaryKey);
    alertConfiguration.setName(name);
    alertConfiguration.setAlertLogicType(alertConfigType);
    alertConfiguration.setStatus(status);
    alertConfiguration.setCreatedWhen(createdWhen);
    alertConfiguration.setCreatedBy(createdBy);
    alertConfiguration.setUpdatedWhen(updatedWhen);
    alertConfiguration.setUpdatedBy(updatedBy);
    return alertConfiguration;
  }

  public static CreateUpdateSandboxAlertConfigRequest getCreateUpdateSandboxAlertConfigRequest() {
    CreateUpdateSandboxAlertConfigRequest request = new CreateUpdateSandboxAlertConfigRequest();
    request.setAlertConfigType(TestUtils.randomAlphanumeric(10));
    request.setName(TestUtils.randomAlphanumeric(5));
    request.setComment(TestUtils.randomAlphanumeric(5));
    request.setWho(TestUtils.randomAlphanumeric(5));
    request.setWhen(TestUtils.randomInstant());
    request.setStatus(CreateUpdateSandboxAlertConfigRequest.StatusEnum.ACTIVE);

    Map<String, String> params = new HashMap<>();
    params.put("key2", "value2");
    params.put("key4", "value4");
    params.put("key1", "value1");
    params.put("key3", "value3");
    request.setAlertParameters(params);

    Map<String, Boolean> logicOverrides = new HashMap<>();
    logicOverrides.put("logic1", true);
    logicOverrides.put("logic2", false);

    request.setLogicOverrideSet(logicOverrides);

    request.setAlertAggregationFields(Arrays.asList("field2", "field1", "field3"));
    AlertFilter filter1 = new AlertFilter();
    filter1.setAggregationFieldName("AggregationField2");
    filter1.setOperation(OperationEnum.IN);
    filter1.setValues(Arrays.asList("v1", "v2"));
    AlertFilter filter2 = new AlertFilter();
    filter2.setAggregationFieldName("AggregationField1");
    filter2.setOperation(OperationEnum.IN);
    filter2.setValues(Arrays.asList("v1", "v2"));
    request.setAlertFilters(Arrays.asList(filter1, filter2));
    return request;
  }

  public static SandboxAlertConfigurationAuditDTO getSandboxConfigurationAuditDTO(
      UUID randomUUID, UUID alertConfigUUID, String name,
      String alertConfigType, String status, String createdBy, Instant createdWhen) {
    SandboxAlertConfigurationAuditDTO alertConfiguration = new SandboxAlertConfigurationAuditDTO();

    SandboxAlertConfigurationAuditDTOPrimaryKey primaryKey = new SandboxAlertConfigurationAuditDTOPrimaryKey(
        randomUUID, alertConfigUUID, TestUtils.randomUUID());

    alertConfiguration.setPrimaryKey(primaryKey);
    alertConfiguration.setName(name);
    alertConfiguration.setAlertLogicType(alertConfigType);
    alertConfiguration.setStatus(status);
    alertConfiguration.setCreatedWhen(createdWhen);
    alertConfiguration.setCreatedBy(createdBy);
    return alertConfiguration;
  }

  public static List<SandboxAlertConfiguration> getSandboxAlertConfigurations() {
    List<SandboxAlertConfiguration> list = new ArrayList<>();
    list.add(getSandboxConfiguration(TestUtils.randomUUID(),
        TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    list.add(getSandboxConfiguration(TestUtils.randomUUID(),
        TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    return list;
  }


  public static SandboxAlertConfiguration getSandboxConfiguration(
      UUID randomUUID, UUID alertConfigUUID, String name,
      String alertConfigType, String status, String createdBy, Instant createdWhen) {
    SandboxAlertConfiguration alertConfiguration = new SandboxAlertConfiguration();

    alertConfiguration.setAlertConfigurationUUID(alertConfigUUID.toString());
    alertConfiguration.setSandboxUUID(randomUUID.toString());
    alertConfiguration.setName(name);
    alertConfiguration.setAlertConfigType(alertConfigType);
    alertConfiguration.setStatus(SandboxAlertConfiguration.StatusEnum.fromValue(status));
    alertConfiguration.setWhen(createdWhen);
    alertConfiguration.setWho(createdBy);
    return alertConfiguration;
  }

  public static List<LiveAlertConfigurationAuditByAlertConfigUUIDDTO> getLiveConfigurationAuditByAlertConfigDTOList() {
    List<LiveAlertConfigurationAuditByAlertConfigUUIDDTO> auditDTOS = new ArrayList<>();
    auditDTOS.add(getLiveConfigurationAuditByAlertConfigDTO(TestUtils.randomUUID(),
        TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    auditDTOS.add(getLiveConfigurationAuditByAlertConfigDTO(TestUtils.randomUUID(),
        TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    ));
    return auditDTOS;
  }

  public static LiveAlertConfigurationAuditByAlertConfigUUIDDTO getLiveConfigurationAuditByAlertConfigDTO(
      UUID id,
      UUID alertConfigUUID, String name, String alertConfigType, String status, String createdBy,
      Instant createdWhen) {
    LiveAlertConfigurationAuditByAlertConfigUUIDDTO auditDTO = new LiveAlertConfigurationAuditByAlertConfigUUIDDTO();

    LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey primaryKey = new LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey();
    primaryKey.setAlertConfigUUID(alertConfigUUID);
    primaryKey.setAuditTimestamp(Instant.now());
    auditDTO.setPrimaryKey(primaryKey);
    auditDTO.setName(name);
    auditDTO.setAlertLogicType(alertConfigType);
    auditDTO.setStatus(status);
    auditDTO.setCreatedWhen(createdWhen);
    auditDTO.setCreatedBy(createdBy);
    return auditDTO;
  }

  public static LiveAlertConfigurationAuditDTO getLiveConfigurationAuditDTO(UUID id,
      UUID alertConfigUUID, String name, String alertConfigType, String status, String createdBy,
      Instant createdWhen) {
    LiveAlertConfigurationAuditDTO auditDTO = new LiveAlertConfigurationAuditDTO();
    auditDTO.setAuditUUID(id);
    auditDTO.setAlertConfigurationUUID(alertConfigUUID);
    auditDTO.setName(name);
    auditDTO.setAlertLogicType(alertConfigType);
    auditDTO.setStatus(status);
    auditDTO.setCreatedWhen(createdWhen);
    auditDTO.setCreatedBy(createdBy);
    return auditDTO;
  }

  public static List<Relationship> getRelationships(UUID fromId) {
    List<Relationship> relationships = new ArrayList<>();
    relationships.add(
        getRelationship(fromId, TestUtils.randomUUID(), EntityType.LIVEALERTCONFIGURATION,
            EntityType.TAG,
            TestUtils.randomAlphanumeric(10)));
    relationships.add(
        getRelationship(fromId, TestUtils.randomUUID(), EntityType.LIVEALERTCONFIGURATION,
            EntityType.TAG,
            TestUtils.randomAlphanumeric(10)));

    return relationships;
  }

  public static Relationship getRelationship(UUID fromId, UUID toId, EntityType fromType,
      EntityType toType, String username) {
    Relationship rel = new Relationship();
    rel.setFromId(fromId);
    rel.setFromType(fromType);
    rel.setToId(toId);
    rel.setToType(toType);
    rel.setUser(username);
    return rel;
  }

  public static EntityRelationship getEntityRelationship(UUID fromId, UUID toId,
      EntityType fromType,
      EntityType toType, String username) {
    EntityRelationship rel = new EntityRelationship();
    rel.setFromId(fromId.toString());
    rel.setFromType(fromType.toString());
    rel.setToId(toId.toString());
    rel.setToType(toType.toString());
    rel.setUser(username);
    return rel;
  }

  public static List<EntityRelationshipAudit> getRelationshipAuditHistory(UUID fromId,
      UUID toId, EntityType fromType, EntityType toType) {
    List<EntityRelationshipAudit> auditHistory = new ArrayList<>();
    auditHistory.add(getEntityRelationshipAudit(fromId, toId, fromType.toString(),
        toType.toString(), EntityRelationshipAudit.OperationEnum.ADD));
    auditHistory.add(getEntityRelationshipAudit(fromId, toId, fromType.toString(),
        toType.toString(), EntityRelationshipAudit.OperationEnum.REMOVE));
    return auditHistory;
  }

  public static List<RelationshipAudit> getRelationshipAudits(UUID fromId, UUID toId,
      EntityType fromType, EntityType toType) {
    List<RelationshipAudit> relationshipAudits = new ArrayList<>();
    relationshipAudits.add(getRelationshipAudit(fromId, toId, fromType, toType,
        EntityRelationshipAudit.OperationEnum.ADD));
    relationshipAudits
        .add(getRelationshipAudit(fromId, toId, fromType, toType,
            EntityRelationshipAudit.OperationEnum.REMOVE));
    return relationshipAudits;
  }

  private static EntityRelationshipAudit getEntityRelationshipAudit(UUID fromId, UUID toId,
      String fromType, String toType, EntityRelationshipAudit.OperationEnum operation) {
    EntityRelationshipAudit audit = new EntityRelationshipAudit();
    audit.setFromId(fromId.toString());
    audit.setToId(toId.toString());
    audit.setOperation(operation);
    audit.setFromType(fromType);
    audit.setToType(toType);
    audit.setUser(TestUtils.randomAlphanumeric(10));
    audit.setWhen(TestUtils.randomInstant());
    return audit;
  }

  private static RelationshipAudit getRelationshipAudit(UUID fromId, UUID toId,
      EntityType fromType, EntityType toType, EntityRelationshipAudit.OperationEnum operation) {
    RelationshipAudit relationshipAudit = new RelationshipAudit();
    relationshipAudit.setFromId(fromId);
    relationshipAudit.setToId(toId);
    relationshipAudit.setFromType(fromType);
    relationshipAudit.setToType(toType);
    relationshipAudit.setOperationWho(TestUtils.randomAlphanumeric(10));
    relationshipAudit.setWhen(TestUtils.randomInstant());
    relationshipAudit.setRelationshipOperation(operation.toString());
    return relationshipAudit;
  }

  public static EntityLinkRequest getEntityLinkRequest(UUID entityId) {
    EntityLinkRequest entityLinkRequest = new EntityLinkRequest();
    entityLinkRequest.setEntityId(entityId.toString());
    return entityLinkRequest;
  }

  public static GuiDeploymentAlertType getDeploymentAlertType(
      CreateUpdateSandboxAlertConfigRequest request) {
    GuiDeploymentAlertType guiType = new GuiDeploymentAlertType();
    guiType.setParameters(request.getAlertParameters().keySet().stream().map(key ->
        getParameter(key)).collect(Collectors.toList()));
    guiType.setLogicOverrides(request.getLogicOverrideSet().keySet().stream().map(key ->
        getLogicOverride(key)).collect(Collectors.toList()));
    return guiType;
  }

  private static LogicOverride getLogicOverride(String id) {
    LogicOverride logicOverride = new LogicOverride();
    logicOverride.setLogicOverrideId(id);
    return logicOverride;
  }

  private static Parameter getParameter(String id) {
    Parameter param = new Parameter();
    param.setParameterId(id);
    return param;
  }
}
