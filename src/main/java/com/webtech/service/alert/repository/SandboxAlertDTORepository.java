package com.webtech.service.alert.repository;

import com.webtech.service.alert.dto.SandboxAlertDTO;
import com.webtech.service.alert.dto.SandboxAlertDTOPrimaryKey;
import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SandboxAlertDTORepository extends
    CassandraRepository<SandboxAlertDTO, SandboxAlertDTOPrimaryKey> {

  @Query("select * from sandbox_alerts where runId IN ?0")
  List<SandboxAlertDTO> findAllByRunIds(List<UUID> runIds);

  List<SandboxAlertDTO> findAllByPrimaryKeyRunId(UUID runId);

}
