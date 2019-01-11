package com.webtech.service.alerttype.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.irisium.service.alertDefinition.model.AlertType;
import com.irisium.service.alertDefinition.model.DeploymentAlertType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.core.io.Resource;

public abstract class BaseAlertTypeConfiguration {

  protected Map<String, AlertType> baseAlertTypes;

  protected Map<String, DeploymentAlertType> deploymentAlertTypes;

  protected void init(Resource baseAlertTypesResource, Resource deploymentAlertTypesResource)
      throws IOException {
    List<AlertType> alertTypes = new ArrayList<>();

    ObjectMapper objectMapper = new ObjectMapper();

    alertTypes.addAll(objectMapper
        .readValue(baseAlertTypesResource.getInputStream(), new TypeReference<List<AlertType>>() {
        }));

    this.baseAlertTypes = new HashMap<>();
    for (AlertType alertType : alertTypes) {
      if (this.baseAlertTypes.get(alertType.getAlertTypeId()) == null) {
        this.baseAlertTypes.put(alertType.getAlertTypeId(), alertType);
      }
    }

    List<DeploymentAlertType> depAlertTypes = new ArrayList<>();

    depAlertTypes.addAll(objectMapper
        .readValue(deploymentAlertTypesResource.getInputStream(),
            new TypeReference<List<DeploymentAlertType>>() {
            }));

    this.deploymentAlertTypes = new HashMap<>();
    for (DeploymentAlertType depAlertType : depAlertTypes) {
      if (this.deploymentAlertTypes.get(depAlertType.getDeploymentAlertTypeId()) == null) {
        this.deploymentAlertTypes.put(depAlertType.getDeploymentAlertTypeId(), depAlertType);
      }
    }
  }

  public AlertType getBaseAlertType(String alertTypeId) throws IOException {
    return getDeepCopy(baseAlertTypes.get(alertTypeId), AlertType.class);
  }

  public Map<String, AlertType> getBaseAlertTypes() throws IOException {
    Map<String, AlertType> deepCopy = new HashMap<>();
    for (Entry<String, AlertType> e : baseAlertTypes.entrySet()) {
      deepCopy.put(e.getKey(), getBaseAlertType(e.getValue().getAlertTypeId()));
    }
    return deepCopy;
  }

  public DeploymentAlertType getDeploymentAlertType(String deploymentAlertTypeId)
      throws IOException {
    return getDeepCopy(deploymentAlertTypes.get(deploymentAlertTypeId), DeploymentAlertType.class);
  }

  public Map<String, DeploymentAlertType> getDeploymentAlertTypes() throws IOException {
    Map<String, DeploymentAlertType> deepCopy = new HashMap<>();
    for (Entry<String, DeploymentAlertType> e : deploymentAlertTypes.entrySet()) {
      deepCopy.put(e.getKey(), getDeploymentAlertType(e.getValue().getDeploymentAlertTypeId()));
    }
    return deepCopy;
  }


  public <T extends Object> T getDeepCopy(T obj, Class<T> clazz) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper
        .readValue(objectMapper.writeValueAsString(obj), clazz);
  }
}
